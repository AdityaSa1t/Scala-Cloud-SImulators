import com.sun.org.apache.xpath.internal.operations.Bool;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simutil.DataCenterUtil;
import simutil.MapperUtil;
import simutil.ReducerUtil;
import simutil.VMUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AllocationTest {

    private static MapperUtil map = new MapperUtil();
    private static ReducerUtil reduce = new ReducerUtil();
    private static List<Vm> vmList;
    private static Logger log = LoggerFactory.getLogger(AllocationTest.class);
    private static Config dataCenterConfig = ConfigFactory.load("DataCenter.conf");
    private static int brokerID;


    private List<Cloudlet> createCloudlets(int brokerId) {
        UtilizationModel cloudletUtilization_1 = new UtilizationModelFull();

        Cloudlet master = new Cloudlet(dataCenterConfig.getInt("MasterCloudlet.index"),
                dataCenterConfig.getLong("MasterCloudlet.length"),
                dataCenterConfig.getInt("MasterCloudlet.pescount"),
                dataCenterConfig.getLong("MasterCloudlet.fileSize"),
                dataCenterConfig.getLong("MasterCloudlet.outputSize"),
                cloudletUtilization_1, cloudletUtilization_1, cloudletUtilization_1);
        master.setUserId(brokerId);

        map.setMapperConfig(cloudletUtilization_1, cloudletUtilization_1, cloudletUtilization_1, brokerId);

        reduce.setReducerConfig(cloudletUtilization_1, cloudletUtilization_1, cloudletUtilization_1, brokerId);

        List<Cloudlet> jobList = new ArrayList<>();
        jobList.add(master);

        int factor= dataCenterConfig.getInt("MapperCloudlet.count")/dataCenterConfig.getInt("ReducerCloudlet.count");
        int lowerBound = 0;
        int upperBound = factor;
        for (int i =0; i<dataCenterConfig.getInt("ReducerCloudlet.count"); i++){
            for(int j=lowerBound; j<upperBound; j++){
                jobList.add(map.getMapperList().get(j));
            }
            jobList.add(reduce.getReducerList().get(i));
            lowerBound = upperBound;
            upperBound = upperBound + factor;
        }

        Cloudlet output = new Cloudlet(dataCenterConfig.getInt("OutputCloudlet.index"),
                dataCenterConfig.getLong("OutputCloudlet.length"),
                dataCenterConfig.getInt("OutputCloudlet.pescount"),
                dataCenterConfig.getLong("OutputCloudlet.fileSize"),
                dataCenterConfig.getLong("OutputCloudlet.outputSize"),
                cloudletUtilization_1, cloudletUtilization_1, cloudletUtilization_1);
        output.setUserId(brokerId);
        jobList.add(output);

        return jobList;
    }

    @Test public void checkSpaceSharedSuccess() throws InterruptedException {

        CloudSim.init(dataCenterConfig.getInt("Simulation.users"), Calendar.getInstance(),
                dataCenterConfig.getBoolean("Simulation.trace_flag"));

        DataCenterUtil DC_Util = new DataCenterUtil();
        Datacenter DC0 = DC_Util.createDataCenterSpaceShared(dataCenterConfig.getString("DataCenter.name1")); //space-shared VM scheduling

        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker(dataCenterConfig.getString("DataCenter.name1"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        brokerID=broker.getId();

        VMUtil vmUtil = new VMUtil();
        vmUtil.setVmList(new CloudletSchedulerSpaceShared(), broker.getId()); //space-shared cloudlet allocation
        broker.submitVmList(vmUtil.getVmList());

        List<Cloudlet> cloudlets = createCloudlets(broker.getId());
        broker.submitCloudletList(cloudlets);

        broker.bindCloudletToVm(cloudlets.get(0).getCloudletId(), vmUtil.getVmList().get(0).getId());

        int factor= dataCenterConfig.getInt("MapperCloudlet.count")/dataCenterConfig.getInt("ReducerCloudlet.count");
        int lowerBound = 0;
        int upperBound = factor;
        for (int i =0; i<dataCenterConfig.getInt("ReducerCloudlet.count"); i++){
            for(int j=lowerBound; j<factor; j++){
                broker.bindCloudletToVm(map.getMapperList().get(j).getCloudletId(), vmUtil.getVmList().get(0).getId());
            }
            broker.bindCloudletToVm(reduce.getReducerList().get(i).getCloudletId(), vmUtil.getVmList().get(0).getId());
            lowerBound = upperBound;
            upperBound = upperBound + factor;
        }

        broker.bindCloudletToVm(cloudlets.get(cloudlets.size()-1).getCloudletId(), vmUtil.getVmList().get(0).getId());

        CloudSim.startSimulation();
        List<Cloudlet> newList = broker.getCloudletSubmittedList();
        CloudSim.stopSimulation();

        Boolean flag = true;
        Cloudlet cloudlet;

        for (int i = 0; i < newList.size(); i++) {
            cloudlet = newList.get(i);

            if (cloudlet.getCloudletStatus() != Cloudlet.SUCCESS) {
                if(cloudlet.getExecStartTime() > newList.get(newList.size()-1).getExecStartTime()){
                    flag = false;
                }
            }
        }
        assert (flag);
    }

    @Test public void checkTimeSharedSuccess() throws InterruptedException {

        CloudSim.init(dataCenterConfig.getInt("Simulation.users"), Calendar.getInstance(),
                dataCenterConfig.getBoolean("Simulation.trace_flag"));

        DataCenterUtil DC_Util = new DataCenterUtil();
        Datacenter DC0 = DC_Util.createDataCenterTimeShared(dataCenterConfig.getString("DataCenter.name1")); //time-shared vm allocation

        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker(dataCenterConfig.getString("DataCenter.name1"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        VMUtil vmUtil = new VMUtil();
        vmUtil.setVmList(new CloudletSchedulerTimeShared(), broker.getId()); //time-shared cloudlet allocation
        broker.submitVmList(vmUtil.getVmList());

        List<Cloudlet> cloudlets = createCloudlets(broker.getId());
        broker.submitCloudletList(cloudlets);

        broker.bindCloudletToVm(cloudlets.get(0).getCloudletId(), vmUtil.getVmList().get(0).getId());

        int factor= dataCenterConfig.getInt("MapperCloudlet.count")/dataCenterConfig.getInt("ReducerCloudlet.count");
        int lowerBound = 0;
        int upperBound = factor;
        for (int i =0; i<dataCenterConfig.getInt("ReducerCloudlet.count"); i++){
            for(int j=lowerBound; j<factor; j++){
                broker.bindCloudletToVm(map.getMapperList().get(j).getCloudletId(), vmUtil.getVmList().get(0).getId());
            }
            broker.bindCloudletToVm(reduce.getReducerList().get(i).getCloudletId(), vmUtil.getVmList().get(0).getId());
            lowerBound = upperBound;
            upperBound = upperBound + factor;
        }

        broker.bindCloudletToVm(cloudlets.get(cloudlets.size()-1).getCloudletId(), vmUtil.getVmList().get(0).getId());

        CloudSim.startSimulation();
        List<Cloudlet> newList = broker.getCloudletSubmittedList();
        CloudSim.stopSimulation();

        Boolean flag = true;
        Cloudlet cloudlet;

        for (int i = 0; i < newList.size(); i++) {
            cloudlet = newList.get(i);

            if (cloudlet.getCloudletStatus() != Cloudlet.SUCCESS) {
                if(cloudlet.getExecStartTime() != newList.get(newList.size()-1).getExecStartTime()){
                    flag = false;
                }
            }
        }
        assert (flag);

    }

    @Test public void testCloudletCreation(){

        List<Cloudlet> cloudlets = createCloudlets(brokerID);
        if(cloudlets.isEmpty() && cloudlets.size()>dataCenterConfig.getInt("MapperCloudlet.count")+dataCenterConfig.getInt("ReducerCloudlet.count")){
            assert (false);
        } else {
            assert (true);
        }
    }

    @Test public void TestVmCreation(){
        VMUtil vmUtil = new VMUtil();
        vmUtil.setVmList(new CloudletSchedulerTimeShared(), brokerID); //time-shared cloudlet allocation
        if(vmUtil.getVmList().isEmpty()){
            assert (false);
        } else {
            assert (true);
        }
    }

    private List<Cloudlet> createReducers(int brokerId) {
        UtilizationModel cloudletUtilization_1 = new UtilizationModelFull();

        Cloudlet output = new Cloudlet(dataCenterConfig.getInt("MasterCloudlet.index"),
                dataCenterConfig.getLong("MasterCloudlet.length"),
                dataCenterConfig.getInt("MasterCloudlet.pescount"),
                dataCenterConfig.getLong("MasterCloudlet.fileSize"),
                dataCenterConfig.getLong("MasterCloudlet.outputSize"),
                cloudletUtilization_1, cloudletUtilization_1, cloudletUtilization_1);
        output.setUserId(brokerId);

        reduce.setReducerConfig(cloudletUtilization_1, cloudletUtilization_1, cloudletUtilization_1, brokerId);

        List<Cloudlet> reducerJobList = new ArrayList<>();
        reducerJobList.addAll(reduce.getReducerList());
        reducerJobList.add(output);

        return reducerJobList;
    }

    private static List<Cloudlet> createMappers(int brokerId) {
        UtilizationModel cloudletUtilization_1 = new UtilizationModelFull();

        Cloudlet master = new Cloudlet(dataCenterConfig.getInt("MasterCloudlet.index"),
                dataCenterConfig.getLong("MasterCloudlet.length"),
                dataCenterConfig.getInt("MasterCloudlet.pescount"),
                dataCenterConfig.getLong("MasterCloudlet.fileSize"),
                dataCenterConfig.getLong("MasterCloudlet.outputSize"),
                cloudletUtilization_1, cloudletUtilization_1, cloudletUtilization_1);
        master.setUserId(brokerId);

        map.setMapperConfig(cloudletUtilization_1, cloudletUtilization_1, cloudletUtilization_1, brokerId);

        List<Cloudlet> mapperJobList = new ArrayList<>();
        mapperJobList.add(master);
        mapperJobList.addAll(map.getMapperList());

        return mapperJobList;
    }

    @Test public void checkMultiDataCenterJob () {
        CloudSim.init(dataCenterConfig.getInt("Simulation.users"), Calendar.getInstance(),
                dataCenterConfig.getBoolean("Simulation.trace_flag"));

        DataCenterUtil DC_Util = new DataCenterUtil();
        Datacenter DC0 = DC_Util.createDataCenterSpaceShared(dataCenterConfig.getString("DataCenter.name1")); //space-shared VM scheduling
        Datacenter DC1 = DC_Util.createDataCenterTimeShared(dataCenterConfig.getString("DataCenter.name2")); //time-shared VM scheduling

        DatacenterBroker broker1 = null;
        DatacenterBroker broker2 = null;
        try {
            broker1 = new DatacenterBroker(dataCenterConfig.getString("DataCenter.name1"));
            broker2 = new DatacenterBroker(dataCenterConfig.getString("DataCenter.name2"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        VMUtil vmUtilMap = new VMUtil();
        vmUtilMap.setHalfVmList(new CloudletSchedulerSpaceShared(), broker1.getId()); //space-shared cloudlet allocation
        broker1.submitVmList(vmUtilMap.getVmList());
        broker1.submitCloudletList(createMappers(broker1.getId()));
        for(int i=0; i<=dataCenterConfig.getInt("MapperCloudlet.count") ; i++){
            broker1.bindCloudletToVm(createMappers(broker1.getId()).get(i).getCloudletId(), vmUtilMap.getVmList().get(0).getId());
        }

        VMUtil vmUtilReduce = new VMUtil();
        vmUtilReduce.setHalfVmList(new CloudletSchedulerTimeShared(), broker2.getId());//time-shared cloudlet allocation
        broker2.submitVmList(vmUtilReduce.getVmList());
        broker2.submitCloudletList(createReducers(broker2.getId()));
        for(int i=0; i<=dataCenterConfig.getInt("ReducerCloudlet.count") ; i++){
            broker2.bindCloudletToVm(createReducers(broker2.getId()).get(i).getCloudletId(), vmUtilReduce.getVmList().get(1).getId());
        }


        CloudSim.startSimulation();
        List<Cloudlet> mapList = broker1.getCloudletSubmittedList();
        List<Cloudlet> reduceList = broker2.getCloudletSubmittedList();
        CloudSim.stopSimulation();

        Boolean flag1 = true;
        Boolean flag2 = true;
        Cloudlet cloudlet;

        for (int i = 0; i < mapList.size(); i++) {
            cloudlet = mapList.get(i);

            if (cloudlet.getCloudletStatus() != Cloudlet.SUCCESS) {
                if(cloudlet.getExecStartTime() > mapList.get(mapList.size()-1).getExecStartTime()){
                    flag1 = false;
                }
            }
        }

        for (int i = 0; i < reduceList.size(); i++) {
            cloudlet = reduceList.get(i);

            if (cloudlet.getCloudletStatus() != Cloudlet.SUCCESS) {
                if(cloudlet.getExecStartTime() != reduceList.get(reduceList.size()-1).getExecStartTime()){
                    flag2 = false;
                }
            }
        }

        assert (flag1 && flag2);

    }



    public static void main(String[] args) {
        Result result1 = JUnitCore.runClasses(AllocationTest.class);
        for (Failure failure : result1.getFailures()) {
            System.out.println(failure.toString());//LOG THIS
        }
        System.out.println("SpaceSharedAllocation Result=="+result1.wasSuccessful());
    }
}

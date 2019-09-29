package simutil;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.inesc_id.gsd.cloud2sim.applications.roundrobin.RoundRobinVmAllocationPolicy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;



public class DataCenterUtil { /*This is a utility class which creates datacenters.*/

    List<Host> hostList = new ArrayList<>();
    List<Pe> peList = new ArrayList<>();

    private static Logger log = LoggerFactory.getLogger(VMUtil.class);
    private static Config dataCenterConfig = ConfigFactory.load("DataCenter.conf");

    public List<Pe> getPeList() {
        for(int i=0; i<dataCenterConfig.getInt("VM.count"); i++){
            /*adding the appropriate number of PEs based on the number of VMs in the config file.*/

            peList.add(new Pe(i, new PeProvisionerSimple(dataCenterConfig.getInt("DataCenter.peProvisioner"))));
            log.debug("PE"+i+" created.");
        }
        log.info("Number of PEs created ="+ peList.size());
        return peList;
    }

    public List<Host> getHostList(VmScheduler vmScheduler) {
        for(int i=0; i<dataCenterConfig.getInt("Host.count"); i++){
            /*adding hosts based on configurations*/

            hostList.add( new Host(i, new RamProvisionerSimple(dataCenterConfig.getInt("Host.ram")),
                    new BwProvisionerSimple(dataCenterConfig.getInt("Host.bw")),
                    dataCenterConfig.getLong("Host.storage"), peList, vmScheduler));
            log.debug("Host-"+i+" created.");
        }
        log.info("Number of Hosts created ="+ hostList.size());
        return hostList;
    }


    public DatacenterCharacteristics getDatacenterCharacteristics(){
        /*Adding data center characteristics from config file.*/

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(dataCenterConfig.getString("DataCenter.architecture"),
                dataCenterConfig.getString("DataCenter.os"),dataCenterConfig.getString("DataCenter.vmm"),
                hostList, dataCenterConfig.getDouble("DataCenter.timezone"),dataCenterConfig.getDouble("DataCenter.computeCostPerSecond"),
                dataCenterConfig.getDouble("DataCenter.costPerMemoryUnit"),dataCenterConfig.getDouble("DataCenter.costPerStorage"),
                dataCenterConfig.getDouble("DataCenter.costPerBw"));

        log.debug("Host Characteristics"+ "\n" + dataCenterConfig.getString("DataCenter.architecture")+ "\n" +
                dataCenterConfig.getString("DataCenter.os")+ "\n" +dataCenterConfig.getString("DataCenter.vmm")+ "\n" +
                hostList+ dataCenterConfig.getDouble("DataCenter.timezone")+ "\n" +dataCenterConfig.getDouble("DataCenter.computeCostPerSecond")+ "\n" +
                dataCenterConfig.getDouble("DataCenter.costPerMemoryUnit")+ "\n" +dataCenterConfig.getDouble("DataCenter.costPerStorage")+ "\n" +
                dataCenterConfig.getDouble("DataCenter.costPerBw"));
        return characteristics;
    }

    public Datacenter createDataCenterTimeShared(String name){

        LinkedList<Storage> storageList = new LinkedList<>();
        Datacenter DC = null;

        try{
            DC = new Datacenter (name, getDatacenterCharacteristics(),
                    new VmAllocationPolicySimple(getHostList(new VmSchedulerTimeShared(getPeList()))),
                    storageList, dataCenterConfig.getInt("DataCenter.delay"));
        } catch (Exception e){
            e.printStackTrace();
        }

        log.info("Datacenter "+DC.getName()+" created. Allocation = Time-Shared");
        return DC;
    }

    public Datacenter createDataCenterSpaceShared(String name){

        LinkedList<Storage> storageList = new LinkedList<>();
        Datacenter DC = null;

        try{
            DC = new Datacenter (name, getDatacenterCharacteristics(),
                    new VmAllocationPolicySimple(getHostList(new VmSchedulerSpaceShared(getPeList()))),
                    storageList, dataCenterConfig.getInt("DataCenter.delay"));
        } catch (Exception e){
            e.printStackTrace();
        }

        log.info("Datacenter "+DC.getName()+" created. Allocation = Space-Shared");
        return DC;
    }

    public Datacenter createDataCenterTimeSharedSubscription(String name){


        LinkedList<Storage> storageList = new LinkedList<>();
        Datacenter DC = null;

        try{
            DC = new Datacenter (name, getDatacenterCharacteristics(),
                    new RoundRobinVmAllocationPolicy(getHostList(new VmSchedulerTimeSharedOverSubscription(getPeList()))),
                    storageList, dataCenterConfig.getInt("DataCenter.delay"));
        } catch (Exception e){
            e.printStackTrace();
        }

        log.info("Datacenter "+DC.getName()+" created. Allocation = Time-Shared over subscription.");
        return DC;
    }



}

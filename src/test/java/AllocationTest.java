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

    private static MapperUtil mapper = new MapperUtil();
    private static ReducerUtil reducer = new ReducerUtil();
    public static VMUtil vmUtil = new VMUtil();
    public static DataCenterUtil dataCenter = new DataCenterUtil();
    private static Logger log = LoggerFactory.getLogger(AllocationTest.class);
    private static int brokerID = 999;

    @Test public void testMapperCreation(){
        mapper.setMapperConfig(new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull(), brokerID);
        if(mapper.getMapperList().isEmpty()){
            assert (false);
        } else {
            assert (true);
        }
    }

    @Test public void testReducerCreation(){
        reducer.setReducerConfig(new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull(), brokerID);
        if(reducer.getReducerList().isEmpty()){
            assert (false);
        } else {
            assert (true);
        }
    }

    @Test public void TestVmCreation(){
        vmUtil.setVmList(new CloudletSchedulerTimeShared(), brokerID); //time-shared cloudlet allocation
        if(vmUtil.getVmList().isEmpty()){
            assert (false);
        } else {
            assert (true);
        }
    }

    @Test public void TestHostListCreation(){
        List<Host> hostList = dataCenter.getHostList(new VmSchedulerSpaceShared(dataCenter.getPeList()));
        if(hostList.isEmpty()){
            assert (false);
        } else {
            assert (true);
        }
    }

    @Test public void TestPeList(){
        if(dataCenter.getPeList().isEmpty()){
            assert (false);
        } else {
            assert (true);
        }
    }

    public static void main(String[] args) {
        Result result1 = JUnitCore.runClasses(AllocationTest.class);
        for (Failure failure : result1.getFailures()) {
            log.warn(failure.toString());
        }
        log.info(" Result=="+result1.wasSuccessful());
    }
}

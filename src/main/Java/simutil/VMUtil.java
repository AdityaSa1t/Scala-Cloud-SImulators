package simutil;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Vm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class VMUtil { /*Utility class to create a list of Virtual Machines from the given config.*/

    List<Vm> vmList = new ArrayList<>();
    private static Logger log = LoggerFactory.getLogger(VMUtil.class);
    private static Config dataCenterConfig = ConfigFactory.load("DataCenter.conf");

    public void setVmList(CloudletScheduler scheduler, int brokerId) {
        for (int i = 0; i < dataCenterConfig.getInt("VM.count"); i++) {
            vmList.add(new Vm(dataCenterConfig.getInt("VM.index") + i, brokerId,
                    dataCenterConfig.getDouble("VM.mips"), dataCenterConfig.getInt("VM.vCPU"),
                    dataCenterConfig.getInt("VM.ram"), dataCenterConfig.getLong("VM.bw"),
                    dataCenterConfig.getLong("VM.storage"), dataCenterConfig.getString("VM.vmm"), scheduler));
            log.debug("VM-"+ i +" added to VM list.");
        }
        log.info("Number of VMs created="+ vmList.size());
    }

    public void setHalfVmList(CloudletScheduler scheduler, int brokerId) {
        /*Used during multi datacenter simulations*/

        for (int i = 0; i < dataCenterConfig.getInt("VM.count")/2; i++) {
            vmList.add(new Vm(dataCenterConfig.getInt("VM.index") + i, brokerId,
                    dataCenterConfig.getDouble("VM.mips"), dataCenterConfig.getInt("VM.vCPU"),
                    dataCenterConfig.getInt("VM.ram"), dataCenterConfig.getLong("VM.bw"),
                    dataCenterConfig.getLong("VM.storage"), dataCenterConfig.getString("VM.vmm"), scheduler));
            log.debug("VM-"+ i +" added to VM list.");
        }
        log.info("Number of VMs created="+ vmList.size());
    }

    public List<Vm> getVmList() {
        return vmList;
    }


}

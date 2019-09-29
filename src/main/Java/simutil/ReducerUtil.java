package simutil;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ReducerUtil { /*Utility class to create a list of Reducer Cloudlets from the given config.*/

    List<Cloudlet> reducerList = new ArrayList<>();
    private static Logger log = LoggerFactory.getLogger(ReducerUtil.class);
    private static Config dataCenterConfig = ConfigFactory.load("DataCenter.conf");

    public void setReducerConfig(UtilizationModel cpu, UtilizationModel ram, UtilizationModel bw, int brokerId){
        for (int i = 0; i < dataCenterConfig.getInt("ReducerCloudlet.count"); i++) {
            reducerList.add(new Cloudlet(dataCenterConfig.getInt("ReducerCloudlet.index") + i,
                    dataCenterConfig.getLong("ReducerCloudlet.length"),
                    dataCenterConfig.getInt("ReducerCloudlet.pescount"),
                    dataCenterConfig.getLong("ReducerCloudlet.fileSize"),
                    dataCenterConfig.getLong("ReducerCloudlet.outputSize"), cpu, ram, bw));
            reducerList.get(i).setUserId(brokerId);
            log.debug("Reducer-"+ i +" added to Reducer Cloudlet list.");
        }
        log.info("Number of Reducer Cloudlets created="+ reducerList.size());
        log.warn("Index Reducer Cloudlets start with 200");
    }

    public List<Cloudlet> getReducerList(){
        return reducerList;
    }
}

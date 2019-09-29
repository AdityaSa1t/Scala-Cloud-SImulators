package simutil;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MapperUtil { /*Utility class to create a list of Mapper Cloudlets from the given config.*/

    List<Cloudlet> mapperList = new ArrayList<>();
    private static Logger log = LoggerFactory.getLogger(MapperUtil.class);
    private static Config dataCenterConfig = ConfigFactory.load("DataCenter.conf");

    public void setMapperConfig(UtilizationModel cpu, UtilizationModel ram, UtilizationModel bw, int brokerId) {
        for (int i = 0; i < dataCenterConfig.getInt("MapperCloudlet.count"); i++) {
            mapperList.add(new Cloudlet(dataCenterConfig.getInt("MapperCloudlet.index") + i,
                    dataCenterConfig.getLong("MapperCloudlet.length"),
                    dataCenterConfig.getInt("MapperCloudlet.pescount"),
                    dataCenterConfig.getLong("MapperCloudlet.fileSize"),
                    dataCenterConfig.getLong("MapperCloudlet.outputSize"), cpu, ram, bw));
            mapperList.get(i).setUserId(brokerId);
            log.debug("Mapper-"+ i +" added to Mapper Cloudlet list.");
        }
        log.info("Number of Mapper Cloudlets created="+ mapperList.size());
        log.warn("Index Mapper Cloudlets start with 100");
    }


    public List<Cloudlet> getMapperList() {
        return mapperList;
    }

}

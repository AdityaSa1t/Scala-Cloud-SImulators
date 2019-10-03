package Simulations

import java.text.DecimalFormat
import java.util
import java.util.Calendar

import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.{Cloudlet, CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared, Datacenter, DatacenterBroker, Log, NetworkTopology, UtilizationModel, UtilizationModelFull}
import org.slf4j.{Logger, LoggerFactory}
import simutil.{DataCenterUtil, MapperUtil, ReducerUtil, VMUtil}

object NetworkScheme {
  private val map: MapperUtil = new MapperUtil
  private val reduce: ReducerUtil = new ReducerUtil
  private val log: Logger = LoggerFactory.getLogger(getClass)
  private val dataCenterConfig: Config = ConfigFactory.load("DataCenter.conf")

  //Creating Reducers and output cloudlets
  private def createReducers(brokerId: Int): util.List[Cloudlet] = {
    val cloudletUtilization_1: UtilizationModel = new UtilizationModelFull
    val output: Cloudlet = new Cloudlet(dataCenterConfig.getInt("OutputCloudlet.index"), dataCenterConfig.getLong("OutputCloudlet.length"), dataCenterConfig.getInt("OutputCloudlet.pescount"), dataCenterConfig.getLong("OutputCloudlet.fileSize"), dataCenterConfig.getLong("OutputCloudlet.outputSize"), cloudletUtilization_1, cloudletUtilization_1, cloudletUtilization_1)
    output.setUserId(brokerId)
    reduce.setReducerConfig(cloudletUtilization_1, cloudletUtilization_1, cloudletUtilization_1, brokerId)
    val reducerJobList: util.List[Cloudlet] = new util.ArrayList[Cloudlet]
    reducerJobList.addAll(reduce.getReducerList)
    reducerJobList.add(output)
    log.info("Added Reducers and Output.")
    reducerJobList
  }

  //Creating Mappers and master cloudlets
  private def createMappers(brokerId: Int): util.List[Cloudlet] = {
    val cloudletUtilization_1: UtilizationModel = new UtilizationModelFull
    val master: Cloudlet = new Cloudlet(dataCenterConfig.getInt("MasterCloudlet.index"), dataCenterConfig.getLong("MasterCloudlet.length"), dataCenterConfig.getInt("MasterCloudlet.pescount"), dataCenterConfig.getLong("MasterCloudlet.fileSize"), dataCenterConfig.getLong("MasterCloudlet.outputSize"), cloudletUtilization_1, cloudletUtilization_1, cloudletUtilization_1)
    master.setUserId(brokerId)
    map.setMapperConfig(cloudletUtilization_1, cloudletUtilization_1, cloudletUtilization_1, brokerId)
    val mapperJobList: util.List[Cloudlet] = new util.ArrayList[Cloudlet]
    mapperJobList.add(master)
    mapperJobList.addAll(map.getMapperList)
    log.info("Added Mappers and Master.")
    mapperJobList
  }

  @throws[InterruptedException]
  def main(args: Array[String]): Unit = {
    CloudSim.init(dataCenterConfig.getInt("Simulation.users"), Calendar.getInstance, dataCenterConfig.getBoolean("Simulation.trace_flag"))
    val DC_Util: DataCenterUtil = new DataCenterUtil

    //Creating Datacenters
    val DC0: Datacenter = DC_Util.createDataCenterSpaceShared(dataCenterConfig.getString("DataCenter.name1")) //space-shared VM scheduling
    val DC1: Datacenter = DC_Util.createDataCenterTimeShared(dataCenterConfig.getString("DataCenter.name2")) //time-shared VM scheduling
    var broker1: DatacenterBroker = null
    var broker2: DatacenterBroker = null
    try {
      broker1 = new DatacenterBroker(dataCenterConfig.getString("DataCenter.name1"))
      broker2 = new DatacenterBroker(dataCenterConfig.getString("DataCenter.name2"))
    } catch {
      case e: Exception =>
        e.printStackTrace()
        log.error("Unable to create brokers.")
    }
    val vmUtilMap: VMUtil = new VMUtil
    vmUtilMap.setHalfVmList(new CloudletSchedulerSpaceShared, broker1.getId) //space-shared cloudlet allocation

    broker1.submitVmList(vmUtilMap.getVmList)
    broker1.submitCloudletList(createMappers(broker1.getId))

    //Adding all mapper cloudlets and master cloudlet. Using while '<=' to account for Master Cloudlet.
    var i: Int = 0
    while ( {
      i <= dataCenterConfig.getInt("MapperCloudlet.count")
    }) {
      broker1.bindCloudletToVm(createMappers(broker1.getId).get(i).getCloudletId, vmUtilMap.getVmList.get(0).getId)

      {
        i += 1; i - 1
      }
    }
    val vmUtilReduce: VMUtil = new VMUtil
    vmUtilReduce.setHalfVmList(new CloudletSchedulerTimeShared, broker2.getId) //time-shared cloudlet allocation

    broker2.submitVmList(vmUtilReduce.getVmList)
    broker2.submitCloudletList(createReducers(broker2.getId))

    //Adding all reducer cloudlets and output cloudlet. Using while '<=' to account for output Cloudlet.
    var j: Int = 0
    while ( {
      j <= dataCenterConfig.getInt("ReducerCloudlet.count")
    }) {
      broker2.bindCloudletToVm(createReducers(broker2.getId).get(j).getCloudletId, vmUtilReduce.getVmList.get(0).getId)

      {
        j += 1; j - 1
      }
    }

    //Setting up network topology. Linking one data center to the other and adding some delay.
    NetworkTopology.addLink(DC0.getId, DC1.getId, dataCenterConfig.getDouble("DataCenter.nwBw"), dataCenterConfig.getDouble("DataCenter.nwDelay"))
    log.info("Setting up network between data centers.")

    CloudSim.startSimulation
    val mapList: util.List[Cloudlet] = broker1.getCloudletSubmittedList.asInstanceOf[util.List[Cloudlet]]
    val reduceList: util.List[Cloudlet] = broker2.getCloudletSubmittedList.asInstanceOf[util.List[Cloudlet]]
    CloudSim.stopSimulation()
    printCloudletList(mapList)
    printCloudletList(reduceList)
  }

  private def printCloudletList(list: util.List[Cloudlet]): Unit = {
    val size: Int = list.size
    var cloudlet: Cloudlet = null
    val indent: String = "    "
    Log.printLine()
    Log.printLine(indent+"========== OUTPUT NETWORK SCHEME =========="+ indent)
    Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Bandwidth Cost" + indent + "Processing Cost")
    val dft: DecimalFormat = new DecimalFormat("###.##")
    var i: Int = 0
    while ( {
      i < size
    }) {
      cloudlet = list.get(i)
      //Log.print(indent + cloudlet.getCloudletId + indent + indent)
      if (cloudlet.getCloudletStatus == Cloudlet.SUCCESS) {
        //Log.print("SUCCESS")
        //Log.printLine(indent + indent + cloudlet.getResourceId + indent + indent + indent + cloudlet.getVmId + indent + indent + dft.format(cloudlet.getActualCPUTime) + indent + indent + dft.format(cloudlet.getExecStartTime) + indent + indent + cloudlet.getAccumulatedBwCost + indent + indent + indent + cloudlet.getProcessingCost)

        /*Below lines are used to get data for individual attributes.*/
        //Log.printLine(dft.format(cloudlet.getActualCPUTime()));
        //Log.printLine(dft.format(cloudlet.getExecStartTime()));
        Log.printLine(dft.format(cloudlet.getFinishTime()));
      }
      {
        i += 1; i - 1
      }
    }
  }
}

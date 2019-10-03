package Simulations

import java.text.DecimalFormat
import java.util
import java.util.Calendar

import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.{Cloudlet, CloudletSchedulerSpaceShared, Datacenter, DatacenterBroker, Log, UtilizationModel, UtilizationModelFull, Vm}
import org.slf4j.{Logger, LoggerFactory}
import simutil.{DataCenterUtil, MapperUtil, ReducerUtil, VMUtil}

object SchemeFour {
  private val map: MapperUtil = new MapperUtil
  private val reduce: ReducerUtil = new ReducerUtil
  private val vmList: util.List[Vm] = null
  private val log: Logger = LoggerFactory.getLogger(SchemeFour.getClass)
  private val dataCenterConfig: Config = ConfigFactory.load("DataCenter.conf")

  private def createCloudlets(brokerId: Int): util.List[Cloudlet] = {
    val cloudletUtilization_1: UtilizationModel = new UtilizationModelFull

    //Creating a master cloudlet
    val master: Cloudlet = new Cloudlet(dataCenterConfig.getInt("MasterCloudlet.index"), dataCenterConfig.getLong("MasterCloudlet.length"), dataCenterConfig.getInt("MasterCloudlet.pescount"), dataCenterConfig.getLong("MasterCloudlet.fileSize"), dataCenterConfig.getLong("MasterCloudlet.outputSize"), cloudletUtilization_1, cloudletUtilization_1, cloudletUtilization_1)
    master.setUserId(brokerId)
    log.debug("Master Cloudlet created.")

    //Creating Mappers
    map.setMapperConfig(cloudletUtilization_1, cloudletUtilization_1, cloudletUtilization_1, brokerId)
    log.debug("Mapper Cloudlets created.")

    //Creating Mappers
    reduce.setReducerConfig(cloudletUtilization_1, cloudletUtilization_1, cloudletUtilization_1, brokerId)
    log.debug("Reducer Cloudlets created.")

    //list to maintain all cloudlets
    val jobList: util.List[Cloudlet] = new util.ArrayList[Cloudlet]
    jobList.add(master)

    /*Here we load each reducer after a certain number of mappers have been added,
    specifically we get the quotient between the number of mappers and reducers and have a Reducer added
    for each 'quotient number' of mappers.*/
    val factor: Int = dataCenterConfig.getInt("MapperCloudlet.count") / dataCenterConfig.getInt("ReducerCloudlet.count")
    var lowerBound: Int = 0
    var upperBound: Int = factor
    var i: Int = 0
    while ( {
      i < dataCenterConfig.getInt("ReducerCloudlet.count")
    }) {
      var j: Int = lowerBound
      while ( {
        j < upperBound
      }) {
        jobList.add(map.getMapperList.get(j))

        {
          j += 1; j - 1
        }
      }
      jobList.add(reduce.getReducerList.get(i))
      lowerBound = upperBound
      upperBound = upperBound + factor

      {
        i += 1; i - 1
      }
    }
    //Creating Output cloudlet.
    val output: Cloudlet = new Cloudlet(dataCenterConfig.getInt("OutputCloudlet.index"), dataCenterConfig.getLong("OutputCloudlet.length"), dataCenterConfig.getInt("OutputCloudlet.pescount"), dataCenterConfig.getLong("OutputCloudlet.fileSize"), dataCenterConfig.getLong("OutputCloudlet.outputSize"), cloudletUtilization_1, cloudletUtilization_1, cloudletUtilization_1)
    output.setUserId(brokerId)
    log.debug("Output Cloudlet created.")
    jobList.add(output)
    jobList
  }

  @throws[InterruptedException]
  def main(args: Array[String]): Unit = {
    CloudSim.init(dataCenterConfig.getInt("Simulation.users"), Calendar.getInstance, dataCenterConfig.getBoolean("Simulation.trace_flag"))
    val DC_Util: DataCenterUtil = new DataCenterUtil

    //Creating a Data Center - Time Shared- VM and Space Shared- Cloudlet
    val DC0: Datacenter = DC_Util.createDataCenterTimeShared(dataCenterConfig.getString("DataCenter.name1")) //time-shared vm allocation
    var broker: DatacenterBroker = null
    try broker = new DatacenterBroker(dataCenterConfig.getString("DataCenter.name1"))
    catch {
      case e: Exception =>
        e.printStackTrace()
        log.error("Unable to create broker.")
    }
    val vmUtil: VMUtil = new VMUtil
    vmUtil.setVmList(new CloudletSchedulerSpaceShared, broker.getId) //space-shared cloudlet allocation

    broker.submitVmList(vmUtil.getVmList)
    val cloudlets: util.List[Cloudlet] = createCloudlets(broker.getId)
    broker.submitCloudletList(cloudlets)

    //Done to bind master cloudlet
    broker.bindCloudletToVm(cloudlets.get(0).getCloudletId, vmUtil.getVmList.get(0).getId)

    /*Here we bind each reducer after a certain number of mappers have been added,
     just like it was done while loading the cloudlets.*/
    val factor: Int = dataCenterConfig.getInt("MapperCloudlet.count") / dataCenterConfig.getInt("ReducerCloudlet.count")
    var lowerBound: Int = 0
    var upperBound: Int = factor
    var i: Int = 0
    while ( {
      i < dataCenterConfig.getInt("ReducerCloudlet.count")
    }) {
      var j: Int = lowerBound
      while ( {
        j < factor
      }) {
        broker.bindCloudletToVm(map.getMapperList.get(j).getCloudletId, vmUtil.getVmList.get(0).getId)

        {
          j += 1; j - 1
        }
      }
      broker.bindCloudletToVm(reduce.getReducerList.get(i).getCloudletId, vmUtil.getVmList.get(0).getId)
      lowerBound = upperBound
      upperBound = upperBound + factor

      {
        i += 1; i - 1
      }
    }
    //Done to bind the output cloudlet
    broker.bindCloudletToVm(cloudlets.get(cloudlets.size - 1).getCloudletId, vmUtil.getVmList.get(0).getId)
    CloudSim.startSimulation
    val newList: util.List[Cloudlet] = broker.getCloudletSubmittedList.asInstanceOf[util.List[Cloudlet]]
    CloudSim.stopSimulation()
    printCloudletList(newList)
  }

  private def printCloudletList(list: util.List[Cloudlet]): Unit = {
    val size: Int = list.size
    var cloudlet: Cloudlet = null
    val indent: String = "    "
    Log.printLine()
    Log.printLine(indent+"========== OUTPUT SCHEME FOUR =========="+indent)
    Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Bandwidth Cost" + indent + "Processing Cost")
    val dft: DecimalFormat = new DecimalFormat("###.##")
    var i: Int = 0
    while ( {
      i < size
    }) {
      cloudlet = list.get(i)
      Log.print(indent + cloudlet.getCloudletId + indent + indent)
      if (cloudlet.getCloudletStatus == Cloudlet.SUCCESS) {
        Log.print("SUCCESS")
        Log.printLine(indent + indent + cloudlet.getResourceId + indent + indent + indent + cloudlet.getVmId + indent + indent + dft.format(cloudlet.getActualCPUTime) + indent + indent + dft.format(cloudlet.getExecStartTime) + indent + indent + cloudlet.getAccumulatedBwCost + indent + indent + indent + cloudlet.getProcessingCost)

        /*Below lines are used to get data for individual attributes.*/
        //Log.printLine(dft.format(cloudlet.getActualCPUTime()));
        //Log.printLine(dft.format(cloudlet.getExecStartTime()));
        //Log.printLine(dft.format(cloudlet.getFinishTime()));
      }

      {
        i += 1; i - 1
      }
    }
  }
}

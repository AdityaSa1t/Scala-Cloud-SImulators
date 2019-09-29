import com.typesafe.config.{Config, ConfigFactory}
import org.junit
import org.junit.Before

class ConfigTest {

  private var dataCenterConfig: Config = null
  @Before def getConfig: Unit = {
    dataCenterConfig = ConfigFactory.load("DataCenter.conf")
  }
  @junit.Test
  def checkFile: Unit = {
    assert(!dataCenterConfig.isEmpty,"Required configurations are present.")
  }

  @junit.Test
   def cloudletCount: Unit = {
    val mapper = dataCenterConfig.getInt("MapperCloudlet.count")
    val reducer = dataCenterConfig.getInt("ReducerCloudlet.count")
    val count: Boolean = mapper>reducer
    assert(count)
  }

  @junit.Test
  def hardwareSanity: Unit = {
    val vmRam = dataCenterConfig.getInt("VM.ram")
    val vmBw = dataCenterConfig.getInt("VM.bw")
    val hostRam = dataCenterConfig.getInt("Host.ram")
    val hostBw = dataCenterConfig.getInt("Host.bw")
    val ramCompare: Boolean = hostRam>vmRam
    val bwCompare: Boolean = hostBw>vmBw
    assert(ramCompare && bwCompare)
  }



}

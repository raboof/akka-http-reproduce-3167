import java.security.{ KeyStore, SecureRandom }
import javax.net.ssl.{ KeyManagerFactory, SSLContext, TrustManagerFactory }

import scala.concurrent.Future
import scala.util.{ Failure, Success }

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpRequest, HttpResponse }
import akka.http.scaladsl.{ ConnectionContext, Http, HttpsConnectionContext }

object Main extends App {
  implicit val system = ActorSystem("reproduce-3167")
  implicit val ec = system.dispatcher

  val httpsService: HttpRequest => Future[HttpResponse] = (request : HttpRequest) => {

  Future.successful(HttpResponse(entity = HttpEntity(
    ContentTypes.`text/html(UTF-8)`,
    "<html><body>Hello https world!</body></html>")))
  
  }
  
  val keyStore: KeyStore = KeyStore.getInstance("PKCS12")
  
  keyStore.load(getClass.getResourceAsStream("/cert_key.p12"), "".toCharArray)
  
  val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("SunX509")
  
  keyManagerFactory.init(keyStore, "".toCharArray)
  val trustManagerFactory : TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
  
  trustManagerFactory.init(keyStore)
  
  val sslContext: SSLContext = SSLContext.getInstance("TLS")
  
  sslContext.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)
  
  val https: HttpsConnectionContext = ConnectionContext.https(sslContext)
  
  Http().bindAndHandleAsync(httpsService, interface = "localhost", port = 8443, connectionContext = https)
  .onComplete {
  
  case Success(binding) =>
    println(s"HTTPS/gRPC server bound to: ${ binding.localAddress }")
  
  case Failure(e) =>
    println(e.getMessage)
    system.terminate()
  
  }
}

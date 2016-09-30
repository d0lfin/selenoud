import ratpack.error.ServerErrorHandler
import ratpack.handling.RequestLogger
import ratpack.http.client.HttpClient
import ru.qatools.selenoud.Cloud
import ru.qatools.selenoud.ErrorHandler
import ru.qatools.selenoud.docker.DockerCloud

import static ratpack.groovy.Groovy.ratpack
import static ru.qatools.selenoud.util.Util.*

ratpack {
    bindings {
        bindInstance(Cloud, DockerCloud.instance)
        bindInstance(ServerErrorHandler, ErrorHandler.instance)
    }

    serverConfig {
        port(intProp('port', '4444'))
        threads(intProp('limit.threads', '200'))
        maxContentLength prop('limit.bodyLength', '10485760') as int
    }

    handlers {
        all RequestLogger.ncsa()

        post("${PREFIX}session") { HttpClient client ->
            registry.get(Cloud).launch(request, response, client)
        }

        path("${PREFIX}session/:sessionId/::.+") { HttpClient client ->
            registry.get(Cloud).proxy(pathTokens.sessionId, request, response, client)
        }

        delete("${PREFIX}session/:sessionId") { HttpClient client ->
            registry.get(Cloud).delete(pathTokens.sessionId, request, response, client)
        }

        get('log/:sessionId') {
            registry.get(Cloud).logs(pathTokens.sessionId, request, response)
        }

        get('status') {
            render(toJson(registry.get(Cloud).status()))
        }

        get('grid/api/hub') {
            render '{}'
        }

        get('ping') {
            render 'OK'
        }

        post('grid/register') {
            render 'ok'
        }

        get('grid/api/proxy') {
            render '{"success": true}'
        }

    }
}

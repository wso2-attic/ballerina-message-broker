package io.ballerina.messaging.broker.client.cmd.impl.revoke;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.ballerina.messaging.broker.client.cmd.impl.grant.GrantCmd;
import io.ballerina.messaging.broker.client.http.HttpClient;
import io.ballerina.messaging.broker.client.http.HttpRequest;
import io.ballerina.messaging.broker.client.http.HttpResponse;
import io.ballerina.messaging.broker.client.output.ResponseFormatter;
import io.ballerina.messaging.broker.client.resources.Configuration;
import io.ballerina.messaging.broker.client.resources.Message;
import io.ballerina.messaging.broker.client.utils.Constants;
import io.ballerina.messaging.broker.client.utils.Utils;

import java.net.HttpURLConnection;

import static io.ballerina.messaging.broker.client.utils.Constants.BROKER_ERROR_MSG;
import static io.ballerina.messaging.broker.client.utils.Constants.HTTP_DELETE;

/**
 * Command representing MB exchange permission revoking.
 */
@Parameters(commandDescription = "Revoke permissions to a exchange in the Broker")
public class RevokeExchangeCmd extends GrantCmd {

    @Parameter(description = "name of the exchange",
               required = true)
    private String exchangeName;

    @Parameter(names = { "--action", "-a" },
               description = "name of the action",
               required = true)
    private String action;

    @Parameter(names = { "--group", "-g" },
               description = "name of the group",
               required = true)
    private String group;

    public RevokeExchangeCmd(String rootCommand) {
        super(rootCommand);
    }

    @Override
    public void execute() {
        if (help) {
            processHelpLogs();
            return;
        }

        Configuration configuration = Utils.getConfiguration(password);
        HttpClient httpClient = new HttpClient(configuration);
        HttpRequest httpRequest = new HttpRequest(Constants.EXCHANGES_URL_PARAM + exchangeName + Constants
                .PERMISSIONS_ACTION_URL_PARAM + action + Constants.PERMISSION_GROUP_URL_PARAM + "/" + group);

        // do DELETE
        HttpResponse response = httpClient.sendHttpRequest(httpRequest, HTTP_DELETE);

        // handle response
        if (response.getStatusCode() == HttpURLConnection.HTTP_OK) {
            Message message = buildResponseMessage(response, "Exchange permission revoked successfully");
            ResponseFormatter.printMessage(message);
        } else {
            ResponseFormatter.handleErrorResponse(buildResponseMessage(response, BROKER_ERROR_MSG));
        }

    }

    @Override
    public void appendUsage(StringBuilder out) {
        out.append("Usage:\n");
        out.append("  " + rootCommand + " revoke queue [queue-name]\n");
    }
}

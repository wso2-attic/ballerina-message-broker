package io.ballerina.messaging.broker.client.cmd.impl.grant;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.ballerina.messaging.broker.client.resources.Permission;
import io.ballerina.messaging.broker.client.utils.Constants;

/**
 * Command representing MB exchange grant permission.
 */
@Parameters(commandDescription = "Grant permissions to a exchange in the Broker")
public class GrantExchangeCmd extends GrantCmd {

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

    public GrantExchangeCmd(String rootCommand) {
        super(rootCommand);
    }

    @Override
    public void execute() {
        if (help) {
            processHelpLogs();
            return;
        }

        Permission permission = new Permission(action, group);

        String urlSuffix = Constants.EXCHANGES_URL_PARAM + exchangeName + Constants.PERMISSIONS_ACTION_URL_PARAM
                + action + Constants.PERMISSION_GROUP_URL_PARAM;

        performResourceCreationOverHttp(urlSuffix,
                                        permission.getUserGroupsAsJsonString(),
                                        "User groups successfully added.");
    }

    @Override
    public void appendUsage(StringBuilder out) {
        out.append("Usage:\n");
        out.append("  " + rootCommand + " grant exchange [exchange-name]\n");
    }
}

package io.ballerina.messaging.broker.client.cmd.impl.grant;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.ballerina.messaging.broker.client.resources.Permission;
import io.ballerina.messaging.broker.client.utils.Constants;

/**
 * Command representing MB queue permission granting.
 */
@Parameters(commandDescription = "Grant permissions to a queue in the Broker")
public class GrantQueueCmd extends GrantCmd {

    @Parameter(description = "name of the queue",
               required = true)
    private String queueName;

    @Parameter(names = { "--action", "-a" },
               description = "name of the action",
               required = true)
    private String action;

    @Parameter(names = { "--group", "-g" },
               description = "name of the group",
               required = true)
    private String group;

    public GrantQueueCmd(String rootCommand) {
        super(rootCommand);
    }

    @Override
    public void execute() {
        if (help) {
            processHelpLogs();
            return;
        }

        Permission permission = new Permission(action, group);

        String urlSuffix = Constants.QUEUES_URL_PARAM + queueName + Constants.PERMISSIONS_ACTION_URL_PARAM + action
                + Constants.PERMISSION_GROUP_URL_PARAM;

        performResourceCreationOverHttp(urlSuffix,
                                        permission.getUserGroupsAsJsonString(),
                                        "User groups successfully added.");
    }

    @Override
    public void appendUsage(StringBuilder out) {
        out.append("Usage:\n");
        out.append("  " + rootCommand + " grant queue [queue-name]\n");
    }
}

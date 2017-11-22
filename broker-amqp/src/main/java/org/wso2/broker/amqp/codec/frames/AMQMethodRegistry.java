/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.broker.amqp.codec.frames;

import org.wso2.broker.amqp.codec.AMQConstant;
import org.wso2.broker.amqp.codec.AMQFrameDecodingException;

/**
 * Keep factory classes for different class IDs and method IDs
 */
public class AMQMethodRegistry {
    public AMQMethodBodyFactory[][] factories = new AMQMethodBodyFactory[101][];

    public AMQMethodRegistry() {
        factories[10] = new AMQMethodBodyFactory[52];

        factories[10][11] = ConnectionStartOk.getFactory();
        factories[10][31] = ConnectionTuneOk.getFactory();
    }

    public AMQMethodBodyFactory getFactory(short classId, short methodId) throws AMQFrameDecodingException {
        try {
            return factories[classId][methodId];
        } catch (NullPointerException e) {
            throw new AMQFrameDecodingException(AMQConstant.COMMAND_INVALID,
                                                "Class " + classId + " unknown in AMQP version 0-91"
                                                        + " (while trying to decode class " + classId + " method "
                                                        + methodId + ".");
        } catch (IndexOutOfBoundsException e) {
            if (classId >= factories.length) {
                throw new AMQFrameDecodingException(AMQConstant.COMMAND_INVALID,
                                                    "Class " + classId + " unknown in AMQP version 0-91"
                                                            + " (while trying to decode class " + classId + " method "
                                                            + methodId + ".");

            } else {
                throw new AMQFrameDecodingException(AMQConstant.COMMAND_INVALID,
                                                    "Method " + methodId + " unknown in AMQP version 0-91"
                                                            + " (while trying to decode class " + classId + " method "
                                                            + methodId + ".");

            }
        }
    }
}

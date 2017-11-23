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

import org.wso2.broker.amqp.codec.AmqConstant;
import org.wso2.broker.amqp.codec.AmqFrameDecodingException;

/**
 * Keep factory classes for different class IDs and method IDs.
 */
public class AmqMethodRegistry {
    public AmqMethodBodyFactory[][] factories = new AmqMethodBodyFactory[101][];

    public AmqMethodRegistry() {
        factories[10] = new AmqMethodBodyFactory[52];
        factories[10][11] = ConnectionStartOk.getFactory();
        factories[10][31] = ConnectionTuneOk.getFactory();
        factories[10][40] = ConnectionOpen.getFactory();
        factories[10][41] = ConnectionOpenOk.getFactory();

        factories[20] = new AmqMethodBodyFactory[42];
        factories[20][10] = ChannelOpen.getFactory();
        factories[20][11] = ChannelOpenOk.getFactory();

        factories[40] = new AmqMethodBodyFactory[24];
        factories[40][10] = ExchangeDeclare.getFactory();
        factories[40][11] = ExchangeDeclareOk.getFactory();

        factories[50] = new AmqMethodBodyFactory[52];
        factories[50][10] = QueueDeclare.getFactory();
        factories[50][11] = QueueDeclareOk.getFactory();

        factories[60] = new AmqMethodBodyFactory[112];
        factories[60][10] = BasicQos.getFactory();
        factories[60][11] = BasicQosOk.getFactory();
    }

    public AmqMethodBodyFactory getFactory(short classId, short methodId) throws AmqFrameDecodingException {
        try {
            return factories[classId][methodId];
        } catch (NullPointerException e) {
            throw new AmqFrameDecodingException(AmqConstant.COMMAND_INVALID,
                                                "Class " + classId + " unknown in AMQP version 0-91"
                                                        + " (while trying to decode class " + classId + " method "
                                                        + methodId + ".");
        } catch (IndexOutOfBoundsException e) {
            if (classId >= factories.length) {
                throw new AmqFrameDecodingException(AmqConstant.COMMAND_INVALID,
                                                    "Class " + classId + " unknown in AMQP version 0-91"
                                                            + " (while trying to decode class " + classId + " method "
                                                            + methodId + ".");

            } else {
                throw new AmqFrameDecodingException(AmqConstant.COMMAND_INVALID,
                                                    "Method " + methodId + " unknown in AMQP version 0-91"
                                                            + " (while trying to decode class " + classId + " method "
                                                            + methodId + ".");

            }
        }
    }
}

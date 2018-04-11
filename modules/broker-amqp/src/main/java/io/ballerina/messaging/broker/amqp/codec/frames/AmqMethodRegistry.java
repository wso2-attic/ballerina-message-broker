/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.messaging.broker.amqp.codec.frames;

import io.ballerina.messaging.broker.amqp.codec.AmqConstant;
import io.ballerina.messaging.broker.amqp.codec.AmqFrameDecodingException;
import io.ballerina.messaging.broker.amqp.codec.auth.AuthenticationStrategy;

/**
 * Keep factory classes for different class IDs and method IDs.
 */
public class AmqMethodRegistry {
    public AmqMethodBodyFactory[][] factories = new AmqMethodBodyFactory[101][];

    public AmqMethodRegistry(AuthenticationStrategy authenticationStrategy) {
        factories[10] = new AmqMethodBodyFactory[52];
        factories[10][11] = ConnectionStartOk.getFactory(authenticationStrategy);
        factories[10][20] = ConnectionSecure.getFactory();
        factories[10][21] = ConnectionSecureOk.getFactory(authenticationStrategy);
        factories[10][31] = ConnectionTuneOk.getFactory();
        factories[10][40] = ConnectionOpen.getFactory();
        factories[10][41] = ConnectionOpenOk.getFactory();
        factories[10][50] = ConnectionClose.getFactory();
        factories[10][51] = ConnectionCloseOk.getFactory();

        factories[20] = new AmqMethodBodyFactory[42];
        factories[20][10] = ChannelOpen.getFactory();
        factories[20][11] = ChannelOpenOk.getFactory();
        factories[20][20] = ChannelFlow.getFactory();
        factories[20][21] = ChannelFlowOk.getFactory();
        factories[20][40] = ChannelClose.getFactory();
        factories[20][41] = ChannelCloseOk.getFactory();

        factories[40] = new AmqMethodBodyFactory[24];
        factories[40][10] = ExchangeDeclare.getFactory();
        factories[40][11] = ExchangeDeclareOk.getFactory();
        factories[40][20] = ExchangeDelete.getFactory();
        factories[40][21] = ExchangeDeleteOk.getFactory();

        factories[50] = new AmqMethodBodyFactory[52];
        factories[50][10] = QueueDeclare.getFactory();
        factories[50][11] = QueueDeclareOk.getFactory();
        factories[50][20] = QueueBind.getFactory();
        factories[50][21] = QueueBindOk.getFactory();
        factories[50][30] = QueuePurge.getFactory();
        factories[50][31] = QueuePurgeOk.getFactory();
        factories[50][40] = QueueDelete.getFactory();
        factories[50][41] = QueueDeleteOk.getFactory();
        factories[50][50] = QueueUnbind.getFactory();
        factories[50][51] = QueueUnbindOk.getFactory();

        factories[60] = new AmqMethodBodyFactory[112];
        factories[60][10] = BasicQos.getFactory();
        factories[60][11] = BasicQosOk.getFactory();
        factories[60][20] = BasicConsume.getFactory();
        factories[60][21] = BasicConsumeOk.getFactory();
        factories[60][30] = BasicCancel.getFactory();
        factories[60][31] = BasicCancelOk.getFactory();
        factories[60][40] = BasicPublish.getFactory();
        factories[60][60] = BasicDeliver.getFactory();
        factories[60][80] = BasicAck.getFactory();
        factories[60][90] = BasicReject.getFactory();
        factories[60][110] = BasicRecover.getFactory();
        factories[60][111] = BasicRecoveryOk.getFactory();

        factories[90] = new AmqMethodBodyFactory[32];
        factories[90][10] = TxSelect.getFactory();
        factories[90][11] = TxSelectOk.getFactory();
        factories[90][20] = TxCommit.getFactory();
        factories[90][21] = TxCommitOk.getFactory();
        factories[90][30] = TxRollback.getFactory();
        factories[90][31] = TxRollbackOk.getFactory();

        factories[100] = new AmqMethodBodyFactory[102];
        factories[100][10] = DtxSelect.getFactory();
        factories[100][20] = DtxStart.getFactory();
        factories[100][21] = DtxStartOk.getFactory();
        factories[100][30] = DtxEnd.getFactory();
        factories[100][31] = DtxEndOk.getFactory();
        factories[100][40] = DtxCommit.getFactory();
        factories[100][41] = DtxCommitOk.getFactory();
        factories[100][50] = DtxForget.getFactory();
        factories[100][51] = DtxForgetOk.getFactory();
        factories[100][70] = DtxPrepare.getFactory();
        factories[100][71] = DtxPrepareOk.getFactory();
        factories[100][80] = DtxRecover.getFactory();
        factories[100][81] = DtxRecoverOk.getFactory();
        factories[100][90] = DtxRollback.getFactory();
        factories[100][91] = DtxRollbackOk.getFactory();
        factories[100][100] = DtxSetTimeout.getFactory();
        factories[100][101] = DtxSetTimeoutOk.getFactory();

    }

    public AmqMethodBodyFactory getFactory(short classId, short methodId) throws AmqFrameDecodingException {
        try {
            AmqMethodBodyFactory factory = factories[classId][methodId];
            if (factory == null) {
                throw new AmqFrameDecodingException(AmqConstant.COMMAND_INVALID,
                                                    "Method " + methodId + " unknown in AMQP version 0-91"
                                                      + " (while trying to decode class " + classId + " method "
                                                      + methodId + ".");
            }

            return factory;
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

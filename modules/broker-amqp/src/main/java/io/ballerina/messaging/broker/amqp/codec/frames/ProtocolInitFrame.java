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

/**
 * AMQP frame used for protocol version negotiation.
 */
public class ProtocolInitFrame {
    public static final ProtocolInitFrame V_091 = new ProtocolInitFrame((byte) 0, (byte) 9, (byte) 1);

    private final byte majorVersion;
    private final byte minorVersion;
    private final byte revision;

    public ProtocolInitFrame(byte majorVersion, byte minorVersion, byte revision) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.revision = revision;
    }

    /**
     * Getter for majorVersion.
     * @return major version
     */
    public byte getMajorVersion() {
        return majorVersion;
    }

    /**
     * Getter for minorVersion.
     * @return minor version
     */
    public byte getMinorVersion() {
        return minorVersion;
    }

    /**
     * Getter for revision.
     * @return revision
     */
    public byte getRevision() {
        return revision;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ProtocolInitFrame)) {
            return false;
        }

        ProtocolInitFrame other = (ProtocolInitFrame) obj;

        return other.majorVersion == this.majorVersion && other.minorVersion == this.minorVersion
                && other.revision == this.revision;
    }

    public int hashCode() {
        return ((0xF & majorVersion) << 8) | ((0xF & minorVersion) << 4) | (0xF & revision);
    }
}

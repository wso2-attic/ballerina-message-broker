/*
* Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import React from 'react';
import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import TableExchanges from './Tables/TableExchanges';
import DialogExchanges from './Dialogs/DialogExchanges';

const styles = (theme) => ({
	title: {
		textAlign: 'left',
		fontSize: 30,
		color: '#284456'
	}
});

/**
 * Construct the component for displaying details of consumers of the broker
 * @class  BodyExchanges
 * @extends {React.Component}
 */

class BodyExchanges extends React.Component {
	render() {
		const { classes } = this.props;
		return (
			<div>
				<div align="right">
					<br />
					<br />
					<Typography className={classes.title}>Exchanges</Typography>
					<br />
					<DialogExchanges />
					<TableExchanges />
				</div>
			</div>
		);
	}
}

BodyExchanges.propTypes = {
	classes: PropTypes.object.isRequired
};

export default withStyles(styles)(BodyExchanges);

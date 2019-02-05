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
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import MessagebrokerImage from '../images/message-broker.png';

const styles = {
	Toolbar: {
		backgroundColor: '#A7B2BA'
	},
	title: {
		color: '#284456',
		marginLeft: 60,
		fontSize: '35px',
		fontfamily: 'sans-serif'
	}
};

/**
 * Construct the Global Navbar header section
 * @class Navbar
 * @extends {React.Component}
 */

class Navbar extends React.Component {
	render() {
		const { classes } = this.props;

		return (
			<div>
				<AppBar position="fixed" color="#284456">
					<Toolbar className={classes.Toolbar}>
						<div>
							{
								<img
									src={MessagebrokerImage}
									alt=""
									height="50"
									width="250"
									style={{ marginLeft: 250 }}
								/>
							}
						</div>
					</Toolbar>
				</AppBar>
			</div>
		);
	}
}

Navbar.propTypes = {
	classes: PropTypes.object.isRequired
};

export default withStyles(styles)(Navbar);

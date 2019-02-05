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
import Navbar from './Base/Header/Navbar';
import Login from './login/Login';

const drawerWidth = 240;

const styles = (theme) => ({
	root: {
		display: 'flex'
	},
	content: {
		marginTop: 70,
		marginLeft: 10,
		marginRight: 20
	}
});

/**
 * Construct the component to render Login page
 * @class Login
 * @extends {React.Component}
 */

class LoginInterface extends React.Component {
	render(props) {
		const { classes } = this.props;

		return (
			<div className={classes.root}>
				<div align="center">
					<Navbar />
				</div>
				<br />

				<main className={classes.content}>
					<div className={classes.toolbar} />
					<Login />
				</main>
			</div>
		);
	}
}

LoginInterface.propTypes = {
	classes: PropTypes.object.isRequired
};

export default withStyles(styles)(LoginInterface);

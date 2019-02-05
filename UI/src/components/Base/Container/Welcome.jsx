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
import Drawer from './Drawer';
import Navbar from '../Header/Navbar';
import { BrowserRouter as Router, Link, Switch } from 'react-router-dom';

const drawerWidth = 240;

const styles = (theme) => ({
	root: {
		display: 'flex'
	},

	drawer: {
		width: drawerWidth,
		flexShrink: 0
	},

	drawerPaper: {
		width: drawerWidth,
		backgroundColor: '#284456'
	},

	toolbar: theme.mixins.toolbar,

	content: {
		flexGrow: 1,
		backgroundColor: ' #E2E5E9',
		padding: theme.spacing.unit * 3
	},

	button: {
		margin: theme.spacing.unit,
		width: 200,
		fontSize: '18px',

		color: 'white',

		textDecoration: 'none',
		font: 'white',
		'&:hover': {
			backgroundColor: '#00897b',
			color: 'white'
		}
	},

	link: {
		textDecoration: 'none'
	},

	addbutton: {
		backgroundColor: '#009688',
		align: 'Right',

		margin: theme.spacing.unit,
		'&:hover': {
			backgroundColor: '#4DB6AC',
			color: 'black'
		}
	},

	input: {
		display: 'none'
	}
});

class Welcome extends React.Component {
	constructor(props) {
		super(props);

		this.state = {
			buttonclicked: false,
			backgroundColor: 'white',
			buttonName: '',
			open: false,
			scroll: 'paper'
		};
	}

	onClickFunction() {}

	render(props) {
		const { classes } = this.props;

		return (
			<div className={classes.root}>
				<div align="center">
					<Navbar />
				</div>

				<Drawer />

				<main className={classes.content}>
					<div className={classes.toolbar} />
					<div>
						<Link to="/home" style={{ color: 'red', fontSize: 20 }}>
							Home
						</Link>
					</div>
				</main>
			</div>
		);
	}
}

Welcome.propTypes = {
	classes: PropTypes.object.isRequired
};

export default withStyles(styles)(Welcome);

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
import Drawer from '@material-ui/core/Drawer';
import Navbar from '../Header/Navbar';
import Tooltip from '@material-ui/core/Tooltip';
import Button from '@material-ui/core/Button';
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
		width: '14%',
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
		marginLeft: 25,
		width: 200,
		height: 40,
		fontSize: '18px',

		textDecoration: 'none',
		font: 'white',
		'&:hover': {
			backgroundColor: 'white',
			color: '#00897b'
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

/**
 * Construct the Global Drawer section
 * @class DrawerInterface
 * @extends {React.Component}
 */

class DrawerInterface extends React.Component {
	constructor(props) {
		super(props);

		this.state = {
			buttonclicked: false,

			buttonName: '',
			open: false,
			scroll: 'paper',
			bgColorExchange: '#00897b',
			bgColorQueue: '#00897b',
			bgColorConsumer: '#00897b',
			bgColorHome: '#00897b',
			foreColorExchange: 'white',
			foreColorQueue: 'white',
			foreColorConsumer: 'white',
			foreColorHome: 'white'
		};
	}

	onClickExchange = () => {
		this.setState({
			bgColorExchange: 'white',
			foreColorExchange: 'black'
		});
	};

	onClickQueue = () => {
		this.setState({
			bgColorQueue: 'white',
			foreColorQueue: 'black'
		});
	};

	onClickConsumer = () => {
		this.setState({
			bgColorConsumer: 'white',
			foreColorConsumer: 'black'
		});
	};
	onClickHome = () => {
		this.setState({
			bgColorHome: 'white',
			foreColorHome: 'black'
		});
	};

	render(props) {
		const { classes } = this.props;

		return (
			<div className={classes.root}>
				<div align="center">
					<Navbar />
				</div>

				<Drawer
					className={classes.drawer}
					variant="permanent"
					classes={{
						paper: classes.drawerPaper
					}}
					anchor="left"
				>
					<div className={classes.toolbar} />

					<Tooltip title="all Exchanges in the broker" enterDelay={300}>
						<Link className={classes.link} to="/exchange">
							<Button
								variant="outlined"
								style={{
									backgroundColor: this.state.bgColorExchange,
									color: this.state.foreColorExchange
								}}
								className={classes.button}
								onClick={this.onClickExchange}
							>
								Exchanges
							</Button>
						</Link>
					</Tooltip>
					<br />
					<Tooltip title="all Queues in the broker">
						<Link className={classes.link} to="/queue">
							<Button
								variant="outlined"
								style={{
									backgroundColor: this.state.bgColorQueue,
									color: this.state.foreColorQueue
								}}
								className={classes.button}
								onClick={this.onClickQueue}
							>
								Queues
							</Button>
						</Link>
					</Tooltip>
					<br />

					<Tooltip title="consumers for a specific queue" enterDelay={300}>
						<Link className={classes.link} to="/consumer">
							<Button
								variant="outlined"
								style={{
									backgroundColor: this.state.bgColorConsumer,
									color: this.state.foreColorConsumer
								}}
								className={classes.button}
								onClick={this.onClickConsumer}
							>
								Consumers
							</Button>
						</Link>
					</Tooltip>
				</Drawer>

				<main className={classes.content}>
					<div className={classes.toolbar} />
				</main>
			</div>
		);
	}
}

DrawerInterface.propTypes = {
	classes: PropTypes.object.isRequired
};

export default withStyles(styles)(DrawerInterface);

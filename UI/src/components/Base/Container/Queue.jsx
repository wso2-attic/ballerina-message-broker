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
import DialogQueues from './Dialogs/DialogQueues';
import TableQueues from './Tables/TableQueues';
import TextField from '@material-ui/core/TextField';
import { Typography } from '@material-ui/core';
import NativeSelect from '@material-ui/core/NativeSelect';
import FormControl from '@material-ui/core/FormControl';

const drawerWidth = 240;

const styles = (theme) => ({
	title: {
		textAlign: 'left',
		fontSize: 30,
		color: '#284456',
		marginLeft: 0
	},
	root: {
		display: 'flex'
	},

	drawer: {
		width: drawerWidth,
		flexShrink: 1
	},

	drawerPaper: {
		width: drawerWidth,
		backgroundColor: '#284456'
	},

	toolbar: theme.mixins.toolbar,

	content: {
		flexGrow: 1,
		backgroundColor: ' #E2E5E9',
		padding: theme.spacing.unit * 3,
		marginLeft: 0
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
	},
	textField: {
		marginLeft: theme.spacing.unit,
		marginRight: theme.spacing.unit
	},

	formControl: {
		marginLeft: 30
	}
});

/**
 * Construct the component for displaying details of queues of the broker
 * @class Queue 
 * @extends {React.Component}
 */

class Queue extends React.Component {
	constructor(props) {
		super(props);

		this.state = {
			buttonclicked: false,
			backgroundColor: 'white',
			buttonName: '',
			open: false,
			scroll: 'paper',
			query: '',
			columnToQuery: ''
		};
	}
	handleUserInput(filterText) {
		this.setState({ filterText: filterText });
	}

	handleChange = (event) => {
		this.setState({ columnToQuery: event.target.value });
	};

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
						<Link to="/queue" style={{ color: 'red', fontSize: 20 }}>
							Queues
						</Link>
					</div>

					<br />
					<br />
					<div>
						<div>
							<Typography className={classes.title}>Queues</Typography>
						</div>
						<br />
						<div align="left">
							<TextField
								id="outlined-text-input"
								label="Search"
								className={classes.textField}
								type="Search"
								margin="normal"
								variant="outlined"
								value={this.props.filterText}
								ref="filterTextInput"
								onChange={(e) => this.setState({ query: e.target.value })}
							/>

							<FormControl className={classes.formControl}>
								<NativeSelect
									value={this.state.columnToQuery}
									onChange={this.handleChange}
									name="age"
									className={classes.selectEmpty}
								>
									<option value="">Select Field</option>
									<option value="Name">Name</option>

									<option value="Durability">Durability</option>

									<option value="autoDelete">autoDelete</option>
								</NativeSelect>
							</FormControl>
						</div>

						<div align="right">
							<DialogQueues />
						</div>
					</div>
					<div align="left">
						<TableQueues data={this.state.query} columnToQuery={this.state.columnToQuery} />
					</div>

					<br />
					<br />
				</main>
			</div>
		);
	}
}

Queue.propTypes = {
	classes: PropTypes.object.isRequired
};

export default withStyles(styles)(Queue);

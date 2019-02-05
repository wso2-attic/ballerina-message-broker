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
import Navbar from '../Header/Navbar';
import { BrowserRouter as Router, Link, Switch } from 'react-router-dom';
import { Typography } from '@material-ui/core';
import Drawer from './Drawer';
import TableConsumers from './Tables/TableConsumers';
import FormControl from '@material-ui/core/FormControl';
import InputLabel from '@material-ui/core/InputLabel';
import Select from '@material-ui/core/Select';
import OutlinedInput from '@material-ui/core/OutlinedInput';
import axios from 'axios';

const drawerWidth = 240;

const styles = (theme) => ({
	title: {
		textAlign: 'left',
		fontSize: 30,
		color: '#284456'
	},
	formControl: {
		margin: theme.spacing.unit,
		minWidth: 120
	},

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
	container: {
		flex: 1,
		flexDirection: 'row',
		justifyContent: 'space-between'
	},

	input: {
		display: 'none'
	}
});

/**
 * Construct the component for displaying details of consumers of a particular queue
 * @class Consumer
 * @extends {React.Component}
 */

class Consumer extends React.Component {
	constructor(props) {
		super(props);
	}

	state = {
		buttonclicked: false,
		backgroundColor: 'white',
		buttonName: '',
		open: false,
		scroll: 'paper',
		consumers: [],
		consumerarray: [],
		data: [],
		value: '',
		queueName: '',
		qName: ''
	};

	onChange = (event) => {
		this.setState({ value: event.target.value });
	};

	componentWillReceiveProps(nextProps) {
		this.state.queueName = nextProps.match.params.name;
	}

	componentDidMount() {
		axios
			.get('/broker/v1.0/queues', {
				withCredentials: true,
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer YWRtaW46YWRtaW4='
				}
			})
			.then((response) => {
				const DATA = [];
				response.data.forEach((element, index) => {
					DATA.push({
						id: index,
						name: element.name,
						type: element.type,
						durability: element.durable.toString(),
						autodelete: element.permissions
					});
				});

				this.setState({ data: DATA });
			})
			.catch(function(error) {});
	}

	handleChange = (event) => {
		this.setState({ value: event.target.value }, () => console.log('valuew', this.state.value));

		const url = `/broker/v1.0/queues/${event.target.value.trim()}/consumers`;

		axios
			.get(url, {
				withCredentials: true,
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer YWRtaW46YWRtaW4='
				}
			})
			.then((response) => {
				const consumers = [];
				response.data.forEach((element, index) => {
					consumers.push({
						id: index,
						isExclusive: element.isExclusive,
						flowEnabled: element.flowEnabled
					});

					this.setState({ consumerarray: consumers });
				});
			})
			.catch(function(error) {});
	};

	render(props) {
		const { classes } = this.props;
		const { data, value } = this.state;

		return (
			<div className={classes.root}>
				<div align="center">
					<Navbar />
				</div>

				<Drawer />

				<main className={classes.content}>
					<div className={classes.toolbar} />
					<div>
						<Link to="/consumer" style={{ color: 'red', fontSize: 20 }}>
							consumer
						</Link>
					</div>

					<br />
					<br />
					<div>
						<Typography className={classes.title}>Consumers</Typography>
					</div>
					<br />
					<div>
						{this.props.match.params.name == undefined ? (
							<div>
								<FormControl variant="outlined" className={classes.formControl}>
									<InputLabel
										ref={(ref) => {
											this.InputLabelRef = ref;
										}}
										htmlFor="outlined-age-simple"
									/>
									<Select
										native
										onChange={this.handleChange}
										value={value}
										input={
											<OutlinedInput
												labelWidth={this.state.labelWidth}
												name="age"
												id="outlined-age-simple"
											/>
										}
									>
										<option value="">Select Queue...</option>
										{data.map((element, index) => (
											<option value={element.name}>{element.name}</option>
										))}
									</Select>
								</FormControl>
								<TableConsumers data={this.state.value} />
							</div>
						) : (
							<div>
								<FormControl variant="outlined" className={classes.formControl}>
									<InputLabel
										ref={(ref) => {
											this.InputLabelRef = ref;
										}}
										htmlFor="outlined-age-simple"
									/>
									<Select
										value={this.props.match.params.name}
										input={
											<OutlinedInput
												labelWidth={this.state.labelWidth}
												name="age"
												id="outlined-age-simple"
											/>
										}
									>
										<option value={this.props.match.params.name}>
											{this.props.match.params.name}
										</option>
										{data.map((element, index) => (
											<option value={element.name}>{element.name}</option>
										))}
									</Select>
								</FormControl>
								<TableConsumers data={this.props.match.params.name} />
							</div>
						)}
					</div>

					<br />

					<br />
					<br />
				</main>
			</div>
		);
	}
}

Consumer.propTypes = {
	classes: PropTypes.object.isRequired
};

export default withStyles(styles)(Consumer);

class SearchBar extends React.Component {
	handleChange() {
		this.props.onUserInput(this.refs.filterTextInput.value);
	}
	render() {
		return (
			<div>
				<input
					type="text"
					placeholder="Search..."
					value={this.props.filterText}
					ref="filterTextInput"
					onChange={this.handleChange.bind(this)}
				/>
			</div>
		);
	}
}

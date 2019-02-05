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
import DialogExchanges from './Dialogs/DialogExchanges';
import TableExchanges from './Tables/TableExchanges';
import TextField from '@material-ui/core/TextField';
import { Typography } from '@material-ui/core';
import Drawer from './Drawer';
import NativeSelect from '@material-ui/core/NativeSelect';
import FormControl from '@material-ui/core/FormControl';

const drawerWidth = 240;

const styles = (theme) => ({
	title: {
		textAlign: 'left',
		fontSize: 30,
		color: '#284456'
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
	},
	formControl: {
		marginLeft: 30
	},
	dialog: {
		width: 100
	}
});

/**
 * Construct the component for displaying details of exchanges of the broker
 * @class Exchange
 * @extends {React.Component}
 */

class Exchange extends React.Component {
	constructor(props) {
		super(props);

		this.state = {
			buttonclicked: false,
			backgroundColor: 'white',
			buttonName: '',
			open: false,
			scroll: 'paper',
			filterText: '',
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
						<Link to="/exchange" style={{ color: 'red', fontSize: 20 }}>
							Exchanges
						</Link>
					</div>

					<br />
					<br />
					<div>
						<Typography className={classes.title}>Exchanges</Typography>
					</div>
					<br />
					<div>
						<div>
							<TextField
								id="outlinedinput"
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
									<option value="Type">Type</option>
									<option value="Durability">Durability</option>
								</NativeSelect>
							</FormControl>
						</div>
						<div />

						<div align="right">
							<DialogExchanges className={classes.dialog} />
						</div>
					</div>

					<br />
					<div align="left">
						<TableExchanges data={this.state.query} columnToQuery={this.state.columnToQuery} />
					</div>

					<br />
					<br />
				</main>
			</div>
		);
	}
}

Exchange.propTypes = {
	classes: PropTypes.object.isRequired
};

export default withStyles(styles)(Exchange);

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

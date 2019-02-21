/*
* Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { withStyles, Grid, TextField, Button, Typography } from '@material-ui/core';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import { Redirect } from 'react-router-dom';
import PropTypes from 'prop-types';
import Snackbar from './Snackbar';

const styles = {
	background: {
		opacity: '.1'
	},
	textField: {
		width: '90%'
	},

	card: {
		minWidth: 275,
		marginTop: 100,
		marginLeft: 500,
		marginRight: 500,
		marginBottom: 300
	},

	bullet: {
		display: 'inline-block',
		margin: '0 2px',
		transform: 'scale(0.8)'
	},
	title: {
		fontSize: 30,
		fontColor: 'white'
	},
	pos: {
		marginBottom: 12
	},
	resize: {
		fontSize: 20
	},
	face: {
		size: 20
	},

	button: {
		borderRadius: '15px',
		fontSize: '18px',
		textTransform: 'none',
		width: '30%',
		height: '30px',
		backgroundColor: '#284456',
		color: 'white',

		'&:hover': {
			backgroundColor: '#00897b',
			color: 'white'
		}
	}
};

/**
 * Construct the login
 * @class Login
 * @extends {React.Component}
 */

class Login extends React.Component {
	constructor(props) {
		super(props);

		this.state = {
			username: '',
			password: '',
			isLoggedIn: 'false',
			host: '',
			port: '',
			showError: false,
			showMissingHost: false,
			showMissingPort: false
		};
	}

	handleInputChange = (event) => {
		const { target } = event;
		const value = target.value;
		const name = target.id;

		this.setState({
			[name]: value
		});
	};

	login = () => {
		if (
			this.state.username == 'admin' &&
			this.state.password == 'admin' &&
			this.state.host != '' &&
			this.state.port != ''
		) {
			this.setState({
				isLoggedIn: true
			});
		} else if (this.state.username != 'admin' || this.state.password != 'admin') {
			this.setState({
				showError: true
			});
		}

		if (this.state.host == '') {
			this.setState({
				showMissingHost: true
			});
		}

		if (this.state.port == '') {
			this.setState({
				showMissingPort: true
			});
		}
		//setting session variables
		sessionStorage.setItem('Host', this.state.host);
		sessionStorage.setItem('Port', this.state.port);
		sessionStorage.setItem('Username', this.state.username);
		sessionStorage.setItem('Password', this.state.password);
	};

	render(props) {
		const { classes } = this.props;
		const { isLoggedIn } = this.state;
		sessionStorage.setItem('isLoggedIn', this.state.isLoggedIn);

		if (isLoggedIn === true) {
			return (
				<Redirect
					to={{
						pathname: '/exchange',

						state: {
							username: this.state.username,
							password: this.state.password,
							host: this.state.host,
							port: this.state.port,
							isLoggedIn: this.state.isLoggedIn
						}
					}}
				/>
			);
		}
		return (
			<div>
				<Card className={classes.card} style={{ backgroundColor: 'rgba(52, 52, 52, 0.3)', width: '35%' }}>
					<CardContent className={classes.cardcontent}>
						<div>
							<Typography
								variant="h5"
								align="center"
								style={{ fontFamily: 'Comic Sans MS', color: 'white', fontSize: '300' }}
							>
								Login
							</Typography>
						</div>
						<br />
						<br />
						{this.state.showError == true ? <Snackbar /> : ''}

						<br />
						<br />
						<Grid container spacing={8} alignItems="flex-end">
							<Grid item md={true} sm={true} xs={true}>
								<TextField
									variant="outlined"
									id="username"
									label="username"
									fullWidth
									margin="normal"
									autoFocus
									required
									style={{ backgroundColor: 'white' }}
									onChange={this.handleInputChange}
									inputStyle={{ textAlign: 'center' }}
									InputProps={{
										classes: {
											input: classes.resize
										}
									}}
								/>
							</Grid>
						</Grid>
						<br />
						<br />
						<Grid container spacing={8} alignItems="flex-end">
							<Grid item md={true} sm={true} xs={true}>
								<TextField
									variant="outlined"
									id="password"
									label="password"
									type="password"
									style={{ backgroundColor: 'white' }}
									fullWidth
									required
									fontColor="white"
									onChange={this.handleInputChange}
									InputProps={{
										classes: {
											input: classes.resize
										}
									}}
								/>
							</Grid>
						</Grid>
						<br />
						<br />
						<br />

						<Grid container spacing={8} alignItems="flex-end">
							<Grid item md={true} sm={true} xs={true}>
								<div>
									<Typography style={{ color: 'white', fontSize: 20 }}>Host</Typography>

									<TextField
										style={{
											backgroundColor: 'white'
										}}
										id="outlinedinput"
										label="host*"
										className={classes.textField}
										type="host"
										margin="normal"
										variant="outlined"
										value={this.props.filterText}
										ref="filterTextInput"
										onChange={(e) =>
											this.setState({
												host: e.target.value
											})}
									/>
								</div>
							</Grid>
							<br />
							<Grid item md={true} sm={true} xs={true}>
								<div>
									<Typography style={{ color: 'white', fontSize: 20 }}>Port</Typography>

									<TextField
										type="number"
										style={{
											backgroundColor: 'white'
										}}
										id="outlinedinput"
										label="port*"
										inputProps={{ min: '0', max: '10', step: '1' }}
										className={classes.textField}
										margin="normal"
										variant="outlined"
										value={this.props.filterText}
										ref="filterTextInput"
										onChange={(e) => this.setState({ port: e.target.value })}
										InputLabelProps={{
											shrink: true
										}}
									/>
								</div>
							</Grid>
							<Grid item />
						</Grid>

						{this.state.showMissingHost == true && this.state.showMissingPort == true ? (
							<Typography variant="h7" style={{ color: '#f44336' }}>
								Please enter host and port
							</Typography>
						) : (
							''
						)}

						{this.state.showMissingHost == true && this.state.showMissingPort != true ? (
							<Typography variant="h7" style={{ color: '#f44336' }}>
								Please enter host
							</Typography>
						) : (
							''
						)}
						{this.state.showMissingPort == true && this.state.showMissingHost != true ? (
							<Typography variant="h7" style={{ color: '#f44336' }}>
								Please enter Port
							</Typography>
						) : (
							''
						)}
						<br />
						<br />
						<br />
						<br />

						<Grid container justify="center" style={{ marginTop: '10px' }}>
							<Button className={classes.button} onClick={this.login}>
								Login
							</Button>
						</Grid>
						<br />
						<br />
					</CardContent>
				</Card>
			</div>
		);
	}
}

Login.propTypes = {
	classes: PropTypes.object.isRequired
};

export default withStyles(styles)(Login);

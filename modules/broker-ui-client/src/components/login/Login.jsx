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
import { withStyles, Grid, TextField, Button, Typography } from '@material-ui/core';
import { Face, Fingerprint } from '@material-ui/icons';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import { Redirect } from 'react-router-dom';
import PropTypes from 'prop-types';

const styles = {
	background: {
		backgroundColor: 'linear-gradient(${red}, ${black})',
		opacity: '.4'
	},
	textField: {
		width: '90%'
	},

	card: {
		minWidth: 275,
		marginTop: 100,
		marginLeft: 500,
		marginRight: 500,
		marginBottom: 300,
		backgroundColor: 'transparent'
	},

	bullet: {
		display: 'inline-block',
		margin: '0 2px',
		transform: 'scale(0.8)'
	},
	title: {
		fontSize: 20
	},
	pos: {
		marginBottom: 12
	},
	resize: {
		fontSize: 20
	},
	face: {
		size: 20
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
			port: ''
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
		if (this.state.username == 'admin' && this.state.password == 'admin') {
			this.setState({
				isLoggedIn: true
			});
		} else {
			window.confirm('incorrect username or password!');
		}
		sessionStorage.setItem('Host', this.state.host);
		sessionStorage.setItem('Port', this.state.port);
		sessionStorage.setItem('Username', this.state.username);
		sessionStorage.setItem('Password', this.state.password);
		sessionStorage.setItem('isLoggedIn', this.state.isLoggedIn);
	};

	render(props) {
		const { classes } = this.props;
		const { isLoggedIn } = this.state;

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
				<Card className={classes.card} style={{ backgroundColor: 'rgba(52, 52, 52, 0.2)' }}>
					<CardContent className={classes.cardcontent}>
						<div>
							<Typography variant="h5" align="center" style={{ color: '#284456', fontSize: '300' }}>
								Login
							</Typography>
						</div>
						<br />
						<Grid container spacing={8} alignItems="flex-end">
							<Grid item>
								<Face styles={{ size: 60 }} />
							</Grid>
							<Grid item md={true} sm={true} xs={true}>
								<TextField
									id="username"
									label="username"
									labelSize="20"
									fullWidth
									autoFocus
									required
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
						<Grid container spacing={8} alignItems="flex-end">
							<Grid item>
								<Fingerprint />
							</Grid>
							<Grid item md={true} sm={true} xs={true}>
								<TextField
									id="password"
									label="password"
									type="password"
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
						<Grid container alignItems="center" justify="space-between">
							<Grid item>
								<div>
									<Typography style={{ color: '#284456' }}>Host</Typography>

									<TextField
										id="outlinedinput"
										label="host"
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
							<Grid item>
								<div>
									<Typography style={{ color: '#284456' }}>Port</Typography>

									<TextField
										id="outlinedinput"
										label="port"
										className={classes.textField}
										type="port"
										margin="normal"
										variant="outlined"
										value={this.props.filterText}
										ref="filterTextInput"
										onChange={(e) => this.setState({ port: e.target.value })}
									/>
								</div>
							</Grid>
							<Grid item />
						</Grid>
						<br />
						<br />
						<br />
						<br />

						<Grid container justify="center" style={{ marginTop: '10px' }}>
							<Button
								variant="outlined"
								style={{
									textTransform: 'none',
									width: 300,
									backgroundColor: '#284456',
									color: 'white'
								}}
								onClick={this.login}
							>
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

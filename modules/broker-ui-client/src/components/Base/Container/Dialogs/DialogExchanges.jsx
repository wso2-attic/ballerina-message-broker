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
import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import DropdownType from './DropDowns/DropdownType';
import DropdownDurability from './DropDowns/DropdownDurability';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import AddIcon from '@material-ui/icons/Add';
import Fab from '@material-ui/core/Fab';
import { Grid, Typography } from '@material-ui/core';
import axios from 'axios';

const styles = (theme) => ({
	button: {
		backgroundColor: '#00897b',
		color: 'white',
		'&:hover': {
			backgroundColor: 'white',
			color: 'black'
		}
	},
	fab: {
		margin: theme.spacing.unit,
		backgroundColor: '#284456'
	}
});
/**
 * Construct the popup window for adding new exchanges to the broker
 * @class  DialogExchanges
 * @extends {React.Component}
 */

class DialogExchanges extends React.Component {
	constructor(props) {
		super(props);

		this.state = {
			exchangeName: '',
			type: '',
			durability: '',
			autoDelete: '',
			showError: false,
			showSuccess: false
		};
	}

	state = {
		open: false,
		color: '#009688'
	};

	handleClickOpen = () => {
		this.setState({ open: true });
	};
	handleClickClose = () => {
		this.setState({ open: false });
	};

	handleClickClear = () => {
		this.setState({
			exchangeName: '',
			showSuccess: false
		});
	};
	handleInputName = (event) => {
		const { target } = event;
		const value = target.value;
		const name = target.id;

		this.setState({
			[name]: value
		});
	};

	handleAdd = () => {
		if (this.state.exchangeName == '' || this.state.type.name == '' || this.state.durability.name == '') {
			{
				this.setState({
					showError: true
				});
			}
		} else {
			let host = sessionStorage.getItem('Host');
			let port = sessionStorage.getItem('Port');
			let username = sessionStorage.getItem('Username');
			let password = sessionStorage.getItem('Password');
			let encodedString = new Buffer(username + ':' + password).toString('base64');

			const url = `https://${host}:${port}/broker/v1.0/exchanges/`;

			axios
				.post(url, {
					withCredentials: true,
					headers: {
						'Content-Type': 'application/json',
						Authorization: `Basic ${encodedString}`
					},
					name: this.state.exchangeName,
					type: this.state.type.name,
					durable: this.state.durability.name
				})
				.then((response) => {
					this.setState({
						showError: false,
						showSuccess: true
					});
				})
				.catch((error) => {
					console.log(error);
				});
		}
	};

	render(props) {
		const { classes } = this.props;

		return (
			<div>
				<Fab aria-label="Add" className={classes.fab} onClick={this.handleClickOpen}>
					<AddIcon style={{ color: 'white' }} />
				</Fab>

				<Dialog
					PaperProps={{
						style: {
							backgroundColor: '#284456',
							boxShadow: 'none'
						}
					}}
					fullWidth={true}
					maxWidth={'sm'}
					open={this.state.open}
					onClose={this.handleClose}
					aria-labelledby="form-dialog-title"
					max-width="105% !important;"
				>
					<DialogTitle id="form-dialog-title" style={{ backgroundColor: '#00897b' }}>
						<Typography variant="h5" style={{ color: 'white' }}>
							Add a new Exchange
						</Typography>
					</DialogTitle>
					<br />
					<br />
					<DialogContent>
						<DialogContentText />
						<Typography variant="h6" style={{ color: 'white' }}>
							Exchange Name
						</Typography>
						<br />
						<TextField
							variant="outlined"
							style={{ backgroundColor: 'white' }}
							autoFocus
							margin="dense"
							id="exchangeName"
							label="Name"
							type="email"
							fullWidth
							onChange={this.handleInputName}
							value={this.state.exchangeName}
						/>
						<br />

						<br />
						<br />
						<DropdownType
							onChange={(type) => {
								this.setState({ type });
							}}
						/>
						<br />

						<br />
						<DropdownDurability
							onChange={(durability) => {
								this.setState({ durability });
							}}
						/>
						<br />
						<br />
						{this.state.showError == true ? (
							<Typography variant="h7" style={{ color: '#f44336' }}>
								Please provide all the details
							</Typography>
						) : (
							''
						)}
						{this.state.showSuccess == true ? (
							<Typography variant="h7" style={{ color: 'white' }}>
								exchange {this.state.exchangeName} created successfully!
							</Typography>
						) : (
							''
						)}
					</DialogContent>
					<DialogActions>
						<Grid container spacing={7}>
							<Grid style={{ margin: '4%' }}>
								<Button onClick={this.handleAdd} className={classes.button}>
									Add Exchange
								</Button>
							</Grid>
							<Grid style={{ margin: '4%' }}>
								<Button onClick={this.handleClickClose} className={classes.button}>
									Cancel
								</Button>
							</Grid>
							<Grid style={{ margin: '4%' }}>
								<Button onClick={this.handleClickClear} className={classes.button}>
									Clear
								</Button>
							</Grid>
						</Grid>
					</DialogActions>
				</Dialog>
			</div>
		);
	}
}

DialogExchanges.propTypes = {
	classes: PropTypes.object.isRequired
};

export default withStyles(styles)(DialogExchanges);

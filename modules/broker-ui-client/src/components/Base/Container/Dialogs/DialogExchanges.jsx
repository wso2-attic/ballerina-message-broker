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
import axios from 'axios';

const styles = (theme) => ({
	button: {
		backgroundColor: '#284456',
		color: 'white'
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
			autoDelete: ''
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
			window.confirm('please provide all the details');
		} else {
			const url = `/broker/v1.0/exchanges/`;

			axios
				.post(url, {
					withCredentials: true,
					headers: {
						'Content-Type': 'application/json',
						Authorization: 'Bearer YWRtaW46YWRtaW4='
					},
					name: this.state.exchangeName,
					type: this.state.type.name,
					durable: this.state.durability.name
				})
				.then(function(response) {})
				.catch(function(error) {});

			window.confirm('exchange' + ' ' + this.state.exchangeName + ' ' + 'created successfully ');
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
					open={this.state.open}
					onClose={this.handleClose}
					aria-labelledby="form-dialog-title"
					max-width="95% !important;"
				>
					<DialogTitle id="form-dialog-title">Add a new Exchange</DialogTitle>
					<DialogContent>
						<DialogContentText />
						<TextField
							autoFocus
							margin="dense"
							id="exchangeName"
							label="Name"
							type="email"
							fullWidth
							onChange={this.handleInputName}
						/>
						<DropdownType
							onChange={(type) => {
								this.setState({ type });
							}}
						/>
						<DropdownDurability
							onChange={(durability) => {
								this.setState({ durability });
							}}
						/>
					</DialogContent>
					<DialogActions>
						<Button onClick={this.handleAdd} className={classes.button}>
							Add Exchange
						</Button>
						<Button onClick={this.handleClickClose} className={classes.button}>
							Cancel
						</Button>
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

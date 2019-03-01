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
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import { Link } from 'react-router-dom';
import { withStyles } from '@material-ui/core/styles';
import PropTypes from 'prop-types';
import { Grid, Typography } from '@material-ui/core';

const styles = (theme) => ({
	button: {
		backgroundColor: '#00897b',
		color: 'white',
		'&:hover': {
			backgroundColor: 'white',
			color: 'black'
		}
	}
});

/**
 * Construct the logout window 
 * @class Logout
 * @extends {React.Component}
 */

class Logout extends React.Component {
	constructor(props) {
		super(props);

		this.state = {};
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

	logout = () => {
		sessionStorage.clear();
	};
	render(props) {
		const { classes } = this.props;

		return (
			<div>
				<Button
					variant="outlined"
					style={{
						textTransform: 'none',
						width: '5%',
						backgroundColor: '#284456',
						color: 'white'
					}}
					onClick={this.handleClickOpen}
				>
					Logout
				</Button>
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
							Logout
						</Typography>
					</DialogTitle>
					<br />
					<br />
					<DialogContent>
						<DialogContentText style={{ color: 'white', fontSize: 20 }}>
							Are you sure you want to logout?
						</DialogContentText>
					</DialogContent>
					<DialogActions>
						<Grid container spacing={7}>
							<Grid style={{ margin: '4%' }}>
								<Link style={{ textDecoration: 'none' }} to="/">
									<Button onClick={this.logout} className={classes.button}>
										Yes
									</Button>
								</Link>
							</Grid>
							<Grid style={{ margin: '4%' }}>
								<Button onClick={this.handleClickClose} className={classes.button}>
									No
								</Button>
							</Grid>
						</Grid>
					</DialogActions>
				</Dialog>
			</div>
		);
	}
}
Logout.propTypes = {
	classes: PropTypes.object.isRequired
};

export default withStyles(styles)(Logout);

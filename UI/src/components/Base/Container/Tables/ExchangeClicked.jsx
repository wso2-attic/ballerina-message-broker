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
import Typography from '@material-ui/core/Typography';
import ExpansionPanelBindings from './specific details/Exchanges/Expansionpanels/Bindings/ExpansionPanelBindings';
import Drawer from '../Drawer';
import { Link } from 'react-router-dom';
import Button from '@material-ui/core/Button';
import axios from 'axios';

const drawerWidth = 240;

const styles = (theme) => ({
	section: {
		margin: '20px'
	},
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
	font: {
		color: '#284456'
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
 * Construct the component for displaying details of a specific exchange
 * @class  ExchangeClicked
 * @extends {React.Component}
 */

class ExchangeClicked extends React.Component {
	constructor(props) {
		super(props);
	}

	state = {
		selected: [],

		data: []
	};

	componentDidMount() {
		const url = `/broker/v1.0/exchanges/${this.props.match.params.name.trim()}`;

		axios
			.get(url, {
				withCredentials: true,
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer YWRtaW46YWRtaW4='
				}
			})
			.then((response) => {
				const DATA = response.data;

				this.setState({ data: DATA });
			})
			.catch(function(error) {});
	}

	render(props) {
		const { classes } = this.props;

		return (
			<div className={classes.root}>
				<div>
					<Drawer />
				</div>
				<div>
					<main className={classes.content}>
						<div className={classes.toolbar} />
						<div className={classes.section}>
							<div>
								<Link to="/exchange" style={{ fontSize: 20 }}>
									Exchanges
								</Link>
								&emsp; &gt; &gt; &emsp;
								<Link to="#" style={{ color: 'red', fontSize: 20 }}>
									{this.props.match.params.name}
								</Link>
							</div>
							<br />
							<br />
							<div>
								<table>
									<tr>
										<Typography variant="h5" className={classes.font} color="#284456">
											<td>Exchange</td>
											<td>:{this.props.match.params.name}</td>
										</Typography>
									</tr>

									<tr>
										<Typography variant="h5" className={classes.font} color="#284456">
											<td>Type</td>
											<td>:{this.state.data.type}</td>
										</Typography>
									</tr>
									<tr>
										<Typography variant="h5" className={classes.font} color="#284456">
											<td>Durabiliy</td>
											<td>:{String(this.state.data.durable)}</td>
										</Typography>
									</tr>
								</table>
							</div>
						</div>
						<br />
						<div align="right" />
						<div width="100%">
							<ExpansionPanelBindings data={this.props.match.params.name} />
							<br />
						</div>
						<br />
						<Link className={classes.link} to="/exchange">
							<Button
								variant="outlined"
								style={{
									backgroundColor: '#284456',
									color: 'white',
									width: '120px'
								}}
								className={classes.button}
							>
								&lt; &lt; Back
							</Button>
						</Link>
					</main>
				</div>
			</div>
		);
	}
}

ExchangeClicked.propTypes = {
	classes: PropTypes.object.isRequired
};

export default withStyles(styles)(ExchangeClicked);

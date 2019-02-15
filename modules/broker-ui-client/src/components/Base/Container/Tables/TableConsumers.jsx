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
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TablePagination from '@material-ui/core/TablePagination';
import TableRow from '@material-ui/core/TableRow';
import FormLabel from '@material-ui/core/FormLabel';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import { lighten } from '@material-ui/core/styles/colorManipulator';
import axios from 'axios';

const rows = [
	{
		id: 'ConsumerId',
		numeric: false,
		disablePadding: true,
		label: 'ConsumerId'
	},

	{
		id: 'IsExclusive',
		numeric: true,
		disablePadding: false,
		label: 'IsExclusive'
	},

	{
		id: 'flowEnabled',
		numeric: true,
		disablePadding: false,
		label: 'flowEnabled'
	},
	{
		id: 'connectionId',
		numeric: true,
		disablePadding: false,
		label: 'connectionId'
	},
	{
		id: 'ChannelId',
		numeric: true,
		disablePadding: false,
		label: 'ChannelId'
	}
];

class EnhancedTableHead extends React.Component {
	render() {
		return (
			<TableHead>
				<TableRow>
					<TableCell padding="checkbox" />
					{rows.map((row) => {
						return (
							<TableCell
								key={row.id}
								numeric={row.numeric}
								padding={row.disablePadding ? 'none' : 'default'}
							>
								<FormLabel>{row.label}</FormLabel>
							</TableCell>
						);
					}, this)}
				</TableRow>
			</TableHead>
		);
	}
}

EnhancedTableHead.propTypes = {
	onSelectAllClick: PropTypes.func.isRequired,
	rowCount: PropTypes.number.isRequired
};

const toolbarStyles = (theme) => ({
	root: {
		paddingRight: theme.spacing.unit
	},
	highlight:
		theme.palette.type === 'light'
			? {
					color: theme.palette.secondary.main,
					backgroundColor: lighten(theme.palette.secondary.light, 0.85)
				}
			: {
					color: theme.palette.text.primary,
					backgroundColor: theme.palette.secondary.dark
				},
	spacer: {
		flex: '1 1 100%'
	},
	actions: {
		color: theme.palette.text.secondary
	},
	title: {
		flex: '0 0 auto'
	}
});

let EnhancedTableToolbar = (props) => {
	const { classes } = props;

	return (
		<Toolbar>
			<div className={classes.title}>
				<Typography variant="h6" id="tableTitle">
					Consumer details
				</Typography>
			</div>
			<div className={classes.spacer} />
			<div className={classes.actions} />
		</Toolbar>
	);
};

EnhancedTableToolbar.propTypes = {
	classes: PropTypes.object.isRequired
};

EnhancedTableToolbar = withStyles(toolbarStyles)(EnhancedTableToolbar);

const styles = (theme) => ({
	root: {
		width: '100%',
		marginTop: theme.spacing.unit * 3
	},
	table: {
		minWidth: 1020
	},
	tableWrapper: {
		overflowX: 'auto'
	},
	tableRow: {
		'&:hover': {
			backgroundColor: '#B2DFDB !important'
		}
	}
});

const newTo = {
	pathname: 'exchangeClicked',
	param1: 'Par1'
};

/**
 * Construct the table for displaying details of consumers of a particular queue
 * @class  TableConsumers
 * @extends {React.Component}
 */

class TableConsumers extends React.Component {
	constructor(props) {
		super(props);
	}
	state = {
		queue: [],
		queName: '',

		data: [],

		page: 0,
		rowsPerPage: 5
	};

	//send get request to retieve details of consumers of the selected queue
	componentWillReceiveProps(nextProps) {
		let host = sessionStorage.getItem('Host');
		let port = sessionStorage.getItem('Port');
		let username = sessionStorage.getItem('Username');
		let password = sessionStorage.getItem('Password');
		let encodedString = new Buffer(username + ':' + password).toString('base64');

		this.state.queName = nextProps.data;

		const url = `https://${host}:${port}/broker/v1.0/queues/${this.props.data.trim()}/consumers`;

		axios
			.get(url, {
				withCredentials: true,
				headers: {
					'Content-Type': 'application/json',
					Authorization: `Basic ${encodedString}`
				}
			})
			.then((response) => {
				const DATA = [];
				response.data.forEach((element, index) => {
					//add the response details to DATA array
					DATA.push({
						id: element.id,
						isExclusive: element.isExclusive.toString(),
						flowEnabled: element.flowEnabled.toString(),
						connectionID: element.transportProperties['connectionId'],

						channelID: element.transportProperties['channelId']
					});
				});

				this.setState({ data: DATA });
			})
			.catch(function(error) {});
	}

	componentDidMount() {}

	handleChangePage = (event, page) => {
		this.setState({ page });
	};

	handleChangeRowsPerPage = (event) => {
		this.setState({ rowsPerPage: event.target.value });
	};

	render() {
		const { classes } = this.props;
		const { data, rowsPerPage, page } = this.state;

		return (
			<Paper className={classes.root}>
				<EnhancedTableToolbar />
				<div className={classes.tableWrapper}>
					<Table className={classes.table} aria-labelledby="tableTitle">
						<EnhancedTableHead onSelectAllClick={this.handleSelectAllClick} rowCount={data.length} />

						<TableBody>
							{data.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage).map((element, index) => {
								return (
									<TableRow
										hover
										className={classes.tableRow}
										onClick={(event) => this.handleClick(event, index)}
										key={index}
										role="checkbox"
										tabIndex={-1}
									>
										<TableCell padding="checkbox" />

										<TableCell component="th" scope="row" padding="10px">
											{element.id}
										</TableCell>
										<TableCell numeric>{element.isExclusive}</TableCell>
										<TableCell numeric>{element.flowEnabled}</TableCell>
										<TableCell numeric>{element.connectionID}</TableCell>
										<TableCell numeric>{element.channelID}</TableCell>
									</TableRow>
								);
							})}
						</TableBody>
					</Table>
				</div>
				<TablePagination
					component="div"
					count={data.length}
					rowsPerPage={rowsPerPage}
					page={page}
					backIconButtonProps={{
						'aria-label': 'Previous Page'
					}}
					nextIconButtonProps={{
						'aria-label': 'Next Page'
					}}
					onChangePage={this.handleChangePage}
					onChangeRowsPerPage={this.handleChangeRowsPerPage}
				/>
			</Paper>
		);
	}
}

TableConsumers.propTypes = {
	classes: PropTypes.object.isRequired
};

export default withStyles(styles)(TableConsumers);

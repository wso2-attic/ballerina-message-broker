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
import { Link } from 'react-router-dom';

const rows = [
	{
		id: 'name',
		numeric: false,
		disablePadding: true,
		label: 'Name'
	},
	{
		id: 'ConsumerCount',
		numeric: true,
		disablePadding: false,
		label: 'ConsumerCount'
	},
	{
		id: 'Durability',
		numeric: true,
		disablePadding: false,
		label: 'Durability'
	},
	{ id: 'Capacity', numeric: true, disablePadding: false, label: 'Capacity' },
	{
		id: 'Size',
		numeric: true,
		disablePadding: false,
		label: 'Size'
	},
	{
		id: 'Auto Delete',
		numeric: true,
		disablePadding: false,
		label: 'Auto Delete'
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
								padding={row.disablePadding ? 'none ' : 'default'}
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
	classes: PropTypes.object.isRequired,

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
					Queue details
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
	},
	tablehead: {},
	tabledetails: {
		fontSize: 15
	}
});

/**
 * Construct the table for displaying details of all queues of the broker
 * @class  TableQueues
 * @extends {React.Component}
 */

class TableQueues extends React.Component {
	state = {
		data: [],
		page: 0,
		rowsPerPage: 5
	};

	//send get request to retieve details of all queues in the broker
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
					//add the response details to DATA array
					DATA.push({
						id: index,
						name: element.name,
						consumerCount: element.consumerCount,
						durability: element.durable.toString(),
						capacity: element.capacity,
						size: element.size,
						autoDelete: element.autoDelete.toString(),
						owner: element.owner,
						permissions: element.permissions
					});
				});

				this.setState({ data: DATA });
			})
			.then((response) => response.data)
			.catch(function(error) {});
	}

	searchingFor = (term) => {
		const columnToQuery = this.props.columnToQuery;

		return function(x) {
			if (columnToQuery == 'Name') {
				return x.name.toLowerCase().includes(term.toLowerCase()) || !term;
			}

			if (columnToQuery == 'Durability') {
				return x.durability.toLowerCase().includes(term.toLowerCase()) || !term;
			}

			if (columnToQuery == 'autoDelete') {
				return x.autoDelete.toLowerCase().includes(term.toLowerCase()) || !term;
			} else {
				return x.name;
			}
		};
	};

	handleChangePage = (event, page) => {
		this.setState({ page });
	};

	handleChangeRowsPerPage = (event) => {
		this.setState({ rowsPerPage: event.target.value });
	};

	render() {
		const { classes } = this.props;
		const { data, rowsPerPage, page } = this.state;
		const emptyRows = rowsPerPage - Math.min(rowsPerPage, data.length - page * rowsPerPage);

		return (
			<Paper className={classes.root}>
				<EnhancedTableToolbar />
				<div className={classes.tableWrapper}>
					<Table className={classes.table} aria-labelledby="tableTitle" pagination={{ pageSize: 5 }}>
						<EnhancedTableHead onSelectAllClick={this.handleSelectAllClick} rowCount={data.length} />
						<TableBody pagination={{ pageSize: 5 }}>
							{data
								.filter(this.searchingFor(this.props.data))
								.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
								.map((element, index) => {
									return (
										<TableRow
											permissions
											hover
											className={classes.tableRow}
											key={index}
											tabIndex={-1}
										>
											<TableCell padding="checkbox" />

											<TableCell component="th" scope="row" padding="none">
												<Link className={classes.tabledetails} to={`/queue/${element.name} `}>
													{element.name}
												</Link>
											</TableCell>
											<TableCell numeric>
												<Link
													to={`/consumer/${element.name} `}
													className={classes.tabledetails}
												>
													{element.consumerCount}{' '}
												</Link>
											</TableCell>
											<TableCell className={classes.tabledetails} numeric>
												{element.durability}
											</TableCell>

											<TableCell className={classes.tabledetails} numeric>
												{element.capacity}
											</TableCell>
											<TableCell className={classes.tabledetails} numeric>
												{element.size}
											</TableCell>
											<TableCell className={classes.tabledetails} numeric>
												{element.autoDelete}
											</TableCell>
										</TableRow>
									);
								})}

							{emptyRows > 0 && (
								<TableRow style={{ height: 49 * emptyRows }}>
									<TableCell colSpan={6} />
								</TableRow>
							)}
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

TableQueues.propTypes = {
	classes: PropTypes.object.isRequired
};

export default withStyles(styles)(TableQueues);

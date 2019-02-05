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
import classNames from 'classnames';
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
import Checkbox from '@material-ui/core/Checkbox';
import { lighten } from '@material-ui/core/styles/colorManipulator';
import axios from 'axios';
import { Link } from 'react-router-dom';

const rows = [
	{
		id: 'Queue',
		numeric: false,
		disablePadding: true,
		label: 'Queue'
	},

	{
		id: 'BindingPattern',
		numeric: true,
		disablePadding: false,
		label: 'BindingPattern'
	}
];

class EnhancedTableHead extends React.Component {
	render() {
		const { onSelectAllClick, numSelected, rowCount } = this.props;

		return (
			<TableHead>
				<TableRow>
					<TableCell padding="checkbox">
						<Checkbox
							indeterminate={numSelected > 0 && numSelected < rowCount}
							checked={numSelected === rowCount}
							onChange={onSelectAllClick}
						/>
					</TableCell>
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
	numSelected: PropTypes.number.isRequired,
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
	const { numSelected, classes } = props;

	return (
		<Toolbar
			className={classNames(classes.root, {
				[classes.highlight]: numSelected > 0
			})}
		>
			<div className={classes.title}>
				{numSelected > 0 ? (
					<Typography color="inherit" variant="subtitle1">
						{numSelected} selected
					</Typography>
				) : (
					<Typography variant="h6" id="tableTitle">
						Bindings
					</Typography>
				)}
			</div>
			<div className={classes.spacer} />
		</Toolbar>
	);
};

EnhancedTableToolbar.propTypes = {
	classes: PropTypes.object.isRequired,
	numSelected: PropTypes.number.isRequired
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
 * Construct the table for adding new bindings to exchanges
 * @class  TableBindings
 * @extends {React.Component}
 */

class TableBindings extends React.Component {
	state = {
		selected: [],

		data: [],

		page: 0,
		rowsPerPage: 5
	};

	componentDidMount() {
		const url = `/broker/v1.0/exchanges/${this.props.data.trim()}/bindings`;

		axios
			.get(url, {
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
						bindingPattern: element.bindingPattern,
						bindings: element.bindings[0]['queueName']
					});
				});

				this.setState({ data: DATA });
			})
			.catch(function(error) {});
	}

	handleSelectAllClick = (event) => {
		if (event.target.checked) {
			this.setState((state) => ({ selected: state.data.map((n) => n.id) }));
			return;
		}
		this.setState({ selected: [] });
	};

	handleClick = (event, id) => {
		const { selected } = this.state;
		const selectedIndex = selected.indexOf(id);
		let newSelected = [];

		if (selectedIndex === -1) {
			newSelected = newSelected.concat(selected, id);
		} else if (selectedIndex === 0) {
			newSelected = newSelected.concat(selected.slice(1));
		} else if (selectedIndex === selected.length - 1) {
			newSelected = newSelected.concat(selected.slice(0, -1));
		} else if (selectedIndex > 0) {
			newSelected = newSelected.concat(selected.slice(0, selectedIndex), selected.slice(selectedIndex + 1));
		}

		this.setState({ selected: newSelected });
	};

	handleChangePage = (event, page) => {
		this.setState({ page });
	};

	handleChangeRowsPerPage = (event) => {
		this.setState({ rowsPerPage: event.target.value });
	};

	isSelected = (id) => this.state.selected.indexOf(id) !== -1;

	render() {
		const { classes } = this.props;
		const { data, selected, page, rowsPerPage } = this.state;

		return (
			<Paper className={classes.root}>
				<EnhancedTableToolbar numSelected={selected.length} />
				<div className={classes.tableWrapper}>
					<Table className={classes.table} aria-labelledby="tableTitle">
						<EnhancedTableHead
							numSelected={selected.length}
							onSelectAllClick={this.handleSelectAllClick}
							rowCount={data.length}
						/>
						<TableBody>
							{data.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage).map((element, index) => {
								const isSelected = this.isSelected(element.id);
								return (
									<TableRow
										hover
										className={classes.tableRow}
										onClick={(event) => this.handleClick(event, index)}
										key={index}
										role="checkbox"
										selected={isSelected}
										tabIndex={-1}
									>
										<TableCell padding="checkbox">
											<Checkbox checked={isSelected} />
										</TableCell>

										<TableCell component="th" scope="row" padding="10px">
											<Link to={`/queue/${element.bindings} `}>{element.bindings}</Link>
										</TableCell>
										<TableCell numeric>{element.bindingPattern}</TableCell>
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

TableBindings.propTypes = {
	classes: PropTypes.object.isRequired
};

export default withStyles(styles)(TableBindings);

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
import InputLabel from '@material-ui/core/InputLabel';
import FormControl from '@material-ui/core/FormControl';
import Select from '@material-ui/core/Select';

const styles = (theme) => ({
	root: {
		display: 'flex',
		flexWrap: 'wrap'
	},
	formControl: {
		margin: theme.spacing.unit,
		minWidth: 120
	},
	selectEmpty: {
		marginTop: theme.spacing.unit * 2
	}
});

/**
 * Construct the DropdownAutodelete list required when adding queues and exchanges to the broker
 * @class DropdownAutodelete
 * @extends {React.Component}
 */

class DropdownAutodelete extends React.Component {
	state = {
		name: '',
		labelWidth: 0
	};

	handleChange = (name) => (event) => {
		const autoDelete = {
			[event.target.name]: event.target.value
		};

		this.setState(autoDelete);
		this.props.onChange(autoDelete);
	};
	render() {
		const { classes } = this.props;

		return (
			<div className={classes.root}>
				<FormControl className={classes.formControl}>
					<InputLabel htmlFor="exchange type">Auto Delete</InputLabel>
					<Select
						native
						value={this.state.age}
						onChange={this.handleChange('name')}
						inputProps={{
							name: 'name'
						}}
					>
						<option value="" />
						<option value={'yes'}>Yes</option>
						<option value={'no'}>No</option>
					</Select>
				</FormControl>
			</div>
		);
	}
}

DropdownAutodelete.propTypes = {
	classes: PropTypes.object.isRequired
};

export default withStyles(styles)(DropdownAutodelete);

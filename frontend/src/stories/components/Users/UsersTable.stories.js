import React from 'react';

import UsersTable from "main/components/Users/UsersTable";
import usersFixtures from 'fixtures/usersFixtures';

export default {
    title: 'components/Users/UsersTable',
    component: UsersTable
};

const Template = (args) => {
    return (
        <UsersTable {...args} />
    )
};

export const Empty = Template.bind({});

Empty.args = {
    users: []
};

export const NoButton = Template.bind({});

NoButton.args = {
    users: usersFixtures.threeUsers
};

export const WithButton = Template.bind({});

WithButton.args = {
    users: usersFixtures.threeUsers,
    showToggleButtons: true
};
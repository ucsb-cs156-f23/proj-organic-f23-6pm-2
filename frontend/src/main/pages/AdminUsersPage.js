import React from "react";
import BasicLayout from "main/layouts/BasicLayout/BasicLayout";
import UsersTable from "main/components/Users/UsersTable"
import { useBackend } from "main/utils/useBackend";

const AdminUsersPage = () => {

    const { data: users, error: _error, status: _status } =
        useBackend(
            // Stryker disable next-line all
            ["/api/admin/users"],
            // Stryker disable next-line StringLiteral,ObjectLiteral
            { method: "GET", url: "/api/admin/users" },
            // Stryker disable next-line all
            []
        );
    const showToggleButtons = false;

    return (
        <BasicLayout>
            <h2>Users</h2>
            <UsersTable users={users} showToggleButtons={showToggleButtons}/>
        </BasicLayout>
    );
};

export default AdminUsersPage;

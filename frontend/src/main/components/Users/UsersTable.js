import React from "react";
import OurTable, { ButtonColumn } from "main/components/OurTable"
import { useBackendMutation } from "main/utils/useBackend";
import { formatTime } from "main/utils/dateUtils";

const columns = [
    {
        Header: 'githubId',
        accessor: 'githubId', // accessor is the "key" in the data
    },
    {
        Header: 'githubLogin',
        accessor: 'githubLogin', // accessor is the "key" in the data
    },
    {
        Header: 'fullName',
        accessor: 'fullName',
    },
    {
        Header: 'Email',
        accessor: 'email',
    },
    {
        Header: 'Last Online',
        id: 'lastOnline',
        accessor: (row) => formatTime(row.lastOnline),
    },
    {
        Header: 'Admin',
        id: 'admin',
        accessor: (row, _rowIndex) => String(row.admin) // hack needed for boolean values to show up
    },
    {
        Header: 'Instructor',
        id: 'instructor',
        accessor: (row, _rowIndex) => String(row.instructor) // hack needed for boolean values to show up
    },
];

export default function UsersTable({ users, showToggleButtons = true}) {
    function cellToAxiosParamsToggleAdmin(cell) {
        return {
            url: "/api/admin/users/toggleAdmin?githubId=" + cell.row.values.githubId,
            method: "POST",
        }
    }
    
    const toggleAdminMutation = useBackendMutation(
        cellToAxiosParamsToggleAdmin,
        {},
        ["/api/admin/users"]
    );
    
    const toggleAdminCallback = async (cell) => { toggleAdminMutation.mutate(cell); }

    function cellToAxiosParamsToggleInstructor(cell) {
        return {
            url: "/api/admin/users/toggleInstructor?githubId=" + cell.row.values.githubId,
            method: "POST",
        }
    }
    
    const toggleInstructorMutation = useBackendMutation(
        cellToAxiosParamsToggleInstructor,
        {},
        ["/api/admin/users"]
    );
    
    const toggleInstructorCallback = async (cell) => { toggleInstructorMutation.mutate(cell); }

    const buttonColumn = [
        ...columns,
        ButtonColumn("toggle-admin", "primary", toggleAdminCallback, "UsersTable"),
        ButtonColumn("toggle-instructor", "primary", toggleInstructorCallback, "UsersTable")
    ]
    if (showToggleButtons) {
        return <OurTable
            data={users}
            columns={buttonColumn}
            testid={"UsersTable"} />;
    }
    else{
        return <OurTable
            data={users}
            columns={columns}
            testid={"UsersTable"} />;
    }
};
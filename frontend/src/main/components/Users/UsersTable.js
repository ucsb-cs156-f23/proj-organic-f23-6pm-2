import {React} from "react";
import {toast} from "react-toastify";
import OurTable, { ButtonColumn } from "main/components/OurTable"
import { useBackendMutation } from "main/utils/useBackend";
import { formatTime } from "main/utils/dateUtils";

export default function UsersTable({ users, showToggleButtons}) {
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

    // Stryker disable all : hard to test for query caching

    const POST = "POST"
    const usr = "/api/admin/users"

    async function toggleAdminSuccess(message = "Toggled Admin") {
        console.log(message);
        toast(message);
        window.location.reload();
    }
    
    async function toggleInstructorSuccess(message = "Toggled Instructor") {
        console.log(message);
        toast(message);
        window.location.reload();
    }

    function cellToAxiosParamsToggleAdmin(cell) {
        return {
            url: usr + "/toggleAdmin",
            method: POST,
            params:{
                githubId: cell.row.values.githubId
            }
        }
    }
    
    const toggleAdminMutation = useBackendMutation(
        cellToAxiosParamsToggleAdmin,
        {onSuccess : toggleAdminSuccess},
        [usr]
    );
    
    const toggleAdminCallback = async (cell) => { toggleAdminMutation.mutate(cell);}

    function cellToAxiosParamsToggleInstructor(cell) {
        return {
            url: usr + "/toggleInstructor",
            method: POST,
            params: {
                githubId: cell.row.values.githubId
            }
        }
    }
    
    const toggleInstructorMutation = useBackendMutation(
        cellToAxiosParamsToggleInstructor,
        {onSuccess : toggleInstructorSuccess},
        [usr]
    );
    
    const toggleInstructorCallback = async (cell) => { toggleInstructorMutation.mutate(cell);}

    // Stryker restore all

    const buttonColumn = [
        ...columns,
        ButtonColumn("toggle-admin", "primary", toggleAdminCallback, "UsersTable"),
        ButtonColumn("toggle-instructor", "primary", toggleInstructorCallback, "UsersTable")
    ]
    const columnsToDisplay = showToggleButtons ? buttonColumn : columns
    return <OurTable
        data={users}
        columns={columnsToDisplay}
        testid={"UsersTable"} />;
};
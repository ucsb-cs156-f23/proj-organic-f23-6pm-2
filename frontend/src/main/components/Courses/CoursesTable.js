import React from "react";
import OurTable, { ButtonColumn } from "main/components/OurTable";

import { useBackendMutation } from "main/utils/useBackend";
import { cellToAxiosParamsDelete, onDeleteSuccess } from "main/utils/coursesUtils"
import { useNavigate } from "react-router-dom";
import { hasRole } from "main/utils/currentUser";

export default function CoursesTable({
    courses,
    currentUser,
    testIdPrefix = "CoursesTable" }) {

    const navigate = useNavigate();

    const editCallback = (cell) => {
        navigate(`/courses/edit/${cell.row.values.id}`)
    }

    // Stryker disable all : hard to test for query caching

    const deleteMutation = useBackendMutation(
        cellToAxiosParamsDelete,
        { onSuccess: onDeleteSuccess },
        ["/api/courses/all"]
    );
    // Stryker restore all 

    // Stryker disable next-line all : TODO try to make a good test for this
    const deleteCallback = async (cell) => { deleteMutation.mutate(cell); }

    const columns = [
        {
            Header: 'id',
            accessor: 'id', // accessor is the "key" in the data
        },

        {
            Header: 'Name',
            accessor: 'name',
        },
        {
            Header: 'School',
            accessor: 'school',
        },
        {
            Header: 'Term',
            accessor: 'term',
        },
        {
            Header: 'Start Date (iso format)',
            accessor: 'startDate',
        },
        {
            Header: 'End Date (iso format)',
            accessor: 'endDate',
        },
        {
            Header: 'GitHub Org',
            accessor: 'githubOrg',
        }
    ];

    if (hasRole(currentUser, "ROLE_ADMIN")) {
        columns.push(ButtonColumn("Edit", "primary", editCallback, testIdPrefix));
        columns.push(ButtonColumn("Delete", "danger", deleteCallback, testIdPrefix));
    } 

    return <OurTable
        data={courses}
        columns={columns}
        testid={testIdPrefix}
    />;
};
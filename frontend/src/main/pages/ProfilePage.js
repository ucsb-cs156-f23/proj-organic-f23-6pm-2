import React from "react";
import BasicLayout from "main/layouts/BasicLayout/BasicLayout";
import ReactJson from "react-json-view";
import UsersTable from "main/components/Users/UsersTable";
import UserEmailsTable from "main/components/Users/UserEmailsTable";
import { useUsers } from "main/utils/users";
import { useCurrentUser } from "main/utils/currentUser";
const ProfilePage = () => {

    const { data: currUser } = useUsers();
    const { data: currentUser } = useCurrentUser();

    // Check if the user is not logged in
    if (!currentUser.loggedIn) {
        return (
            <p>Not logged in.</p>
        )
    }

    // Find the user in currUser array with matching githubId
    const foundUser = currUser.find(user => user.githubId === currentUser.root.user.githubId);

    // If a user with matching githubId is found, update displayUser's lastOnline
    const displayUser = { ...currentUser };
    if (foundUser) {
        displayUser.root.user.lastOnline = foundUser.lastOnline;
    }


    return (
        <BasicLayout>
            <h1 className={"mb-3"}>
                User Profile for {currentUser.root.user.githubLogin}
            </h1>
            <UsersTable users={[displayUser.root.user]}/>
            <h2 className={"mt-3 mb-3"}>
                Emails
            </h2>
            <UserEmailsTable emails={currentUser.root.user.emails} />
            <h2 className={"mt-3 mb-3"}>
                Debugging Information
            </h2>
            <ReactJson src={currentUser} />
        </BasicLayout>
    );
};

export default ProfilePage;

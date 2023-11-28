import { fireEvent, render, screen } from "@testing-library/react";
import UsersTable from "main/components/Users/UsersTable";
import { formatTime } from "main/utils/dateUtils";
import usersFixtures from "fixtures/usersFixtures";
import { QueryClient, QueryClientProvider } from "react-query";

describe("UserTable tests", () => {
    const queryClient = new QueryClient();

    /*test("renders without crashing for empty table without toggle buttons", () => {
        render(
            <QueryClientProvider client={queryClient}>
                <UsersTable users={[]} showToggleButtons={false} />
            </QueryClientProvider>
        );
    });

    */
    test("renders without crashing for empty table", () => {
        render(
            <QueryClientProvider client={queryClient}>
                <UsersTable users={[]} />
            </QueryClientProvider>
        );
    });

    test("renders without crashing for three users", () => {
        render(
            <QueryClientProvider client={queryClient}>
                <UsersTable users={usersFixtures.threeUsers} />
            </QueryClientProvider>
        );
    });

    /*
    test("Has the expected colum headers and content", () => {
        render(
            <QueryClientProvider client={queryClient}>
                <UsersTable users={usersFixtures.threeUsers}/>
            </QueryClientProvider>
        );
    
        const expectedHeaders = ["githubId", "githubLogin", "fullName", "Email", "Last Online", "Admin", "Instructor"];
        const expectedFields = ["githubId", "githubLogin", "fullName", "email", "lastOnline", "admin", "instructor"];
        const testId = "UsersTable";

        expectedHeaders.forEach( (headerText)=> {
            const header = screen.getByText(headerText);
            expect(header).toBeInTheDocument();
        });

        expectedFields.forEach( (field)=> {
          const header = screen.getByTestId(`${testId}-cell-row-0-col-${field}`);
          expect(header).toBeInTheDocument();
        });

        expect(screen.getByTestId(`${testId}-cell-row-0-col-githubId`)).toHaveTextContent("11111");
        expect(screen.getByTestId(`${testId}-cell-row-0-col-admin`)).toHaveTextContent("true");
        expect(screen.getByTestId(`${testId}-cell-row-0-col-fullName`)).toHaveTextContent("Phill Conrad");
        expect(screen.getByTestId(`${testId}-cell-row-0-col-lastOnline`)).toHaveTextContent(formatTime(usersFixtures.threeUsers[0].lastOnline));
        expect(screen.getByTestId(`${testId}-cell-row-1-col-githubLogin`)).toHaveTextContent("cgaucho");
        expect(screen.getByTestId(`${testId}-cell-row-1-col-admin`)).toHaveTextContent("false");
        expect(screen.getByTestId(`${testId}-cell-row-1-col-instructor`)).toHaveTextContent("true");
      });

      test("Toggle Admin button works", () => {
        render(
            <QueryClientProvider client={queryClient}>
                <UsersTable users={usersFixtures.threeUsers}/>
            </QueryClientProvider>
        );
    
        const testId = "UsersTable";
        const toggleAdminButton = screen.getByTestId(`${testId}-cell-row-0-col-toggle-admin-button`);
        expect(toggleAdminButton).toBeInTheDocument();
        expect(toggleAdminButton).toHaveTextContent("toggle-admin");
        fireEvent.click(toggleAdminButton);

      })

      test("Toggle Instructor button works", () => {
        render(
            <QueryClientProvider client={queryClient}>
                <UsersTable users={usersFixtures.threeUsers}/>
            </QueryClientProvider>
        );
    
        const testId = "UsersTable";
        const toggleInstructorButton = screen.getByTestId(`${testId}-cell-row-0-col-toggle-instructor-button`);
        expect(toggleInstructorButton).toBeInTheDocument();
        expect(toggleInstructorButton).toHaveTextContent("toggle-instructor");
        fireEvent.click(toggleInstructorButton);
      })
      */
});

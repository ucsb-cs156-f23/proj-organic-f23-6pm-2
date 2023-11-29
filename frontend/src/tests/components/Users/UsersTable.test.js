import { fireEvent, render, screen } from "@testing-library/react";
import UsersTable from "main/components/Users/UsersTable";
import { formatTime } from "main/utils/dateUtils";
import usersFixtures from "fixtures/usersFixtures";
import { QueryClient, QueryClientProvider } from "react-query";

const mockedNavigate = jest.fn();

jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useNavigate: () => mockedNavigate
}));

describe("UserTable tests", () => {
    const queryClient = new QueryClient();
    const expectedHeaders = ["githubId", "githubLogin", "fullName", "Email", "Last Online", "Admin", "Instructor"];
    const expectedFields = ["githubId", "githubLogin", "fullName", "email", "lastOnline", "admin", "instructor"];
    const testId = "UsersTable";

    test("renders without crashing for empty table without toggle buttons", () => {
        render(
            <QueryClientProvider client={queryClient}>
                <UsersTable users={[]} showToggleButtons={false}/>
            </QueryClientProvider>
        );

        expectedHeaders.forEach((headerText) => {
            const header = screen.getByText(headerText);
            expect(header).toBeInTheDocument();
        });

        expectedFields.forEach((field) => {
            const fieldElement = screen.queryByTestId(`${testId}-cell-row-0-col-${field}`);
            expect(fieldElement).not.toBeInTheDocument();
        });
    });

    test("renders without crashing for three users without toggle buttons", () => {
        render(
            <QueryClientProvider client={queryClient}>
                <UsersTable users={usersFixtures.threeUsers} />
            </QueryClientProvider>
        );
        expectedHeaders.forEach((headerText) => {
            const header = screen.getByText(headerText);
            expect(header).toBeInTheDocument();
        });
        expectedFields.forEach((field) => {
            const header = screen.getByTestId(`${testId}-cell-row-0-col-${field}`);
            expect(header).toBeInTheDocument();
        });
    });

    test("Has the expected column headers and content when toggle is false", () => {
        render(
            <QueryClientProvider client={queryClient}>
                <UsersTable users={usersFixtures.threeUsers} />
            </QueryClientProvider>
        );

        expectedHeaders.forEach((headerText) => {
            const header = screen.getByText(headerText);
            expect(header).toBeInTheDocument();
        });

        expectedFields.forEach((field) => {
            const header = screen.getByTestId(`${testId}-cell-row-0-col-${field}`);
            expect(header).toBeInTheDocument();
        });

        expect(screen.getByTestId(`${testId}-cell-row-0-col-githubId`)).toHaveTextContent("11111");
        expect(screen.getByTestId(`${testId}-cell-row-0-col-admin`)).toHaveTextContent("true");
        expect(screen.getByTestId(`${testId}-cell-row-0-col-instructor`)).toHaveTextContent("false");
        expect(screen.getByTestId(`${testId}-cell-row-0-col-fullName`)).toHaveTextContent("Phill Conrad");
        expect(screen.getByTestId(`${testId}-cell-row-0-col-lastOnline`)).toHaveTextContent(formatTime(usersFixtures.threeUsers[0].lastOnline));
        expect(screen.getByTestId(`${testId}-cell-row-1-col-githubLogin`)).toHaveTextContent("cgaucho");
        expect(screen.queryByText("toggle-admin")).not.toBeInTheDocument();
        expect(screen.queryByText("toggle-instructor")).not.toBeInTheDocument();
    });

    test("Has the expected column headers and content when toggle is true", () => {
        const showToggleButtons = true
        render(
            <QueryClientProvider client={queryClient}>
                <UsersTable users={usersFixtures.threeUsers} showToggleButtons={showToggleButtons} />
            </QueryClientProvider>
        );


        expectedHeaders.forEach((headerText) => {
            const header = screen.getByText(headerText);
            expect(header).toBeInTheDocument();
        });

        expectedFields.forEach((field) => {
            const header = screen.getByTestId(`${testId}-cell-row-0-col-${field}`);
            expect(header).toBeInTheDocument();
        });

        expect(screen.getByTestId(`${testId}-cell-row-0-col-githubId`)).toHaveTextContent("11111");
        expect(screen.getByTestId(`${testId}-cell-row-0-col-admin`)).toHaveTextContent("true");
        expect(screen.getByTestId(`${testId}-cell-row-0-col-instructor`)).toHaveTextContent("false");
        expect(screen.getByTestId(`${testId}-cell-row-0-col-fullName`)).toHaveTextContent("Phill Conrad");
        expect(screen.getByTestId(`${testId}-cell-row-0-col-lastOnline`)).toHaveTextContent(formatTime(usersFixtures.threeUsers[0].lastOnline));
        expect(screen.getByTestId(`${testId}-cell-row-1-col-githubLogin`)).toHaveTextContent("cgaucho");
        expect(screen.getByTestId(`${testId}-cell-row-1-col-admin`)).toHaveTextContent("false");
        expect(screen.getByTestId(`${testId}-cell-row-1-col-instructor`)).toHaveTextContent("true");

        const toggleAdmin = screen.getByTestId(`${testId}-cell-row-0-col-toggle-admin-button`);
        expect(toggleAdmin).toBeInTheDocument();
        expect(toggleAdmin).toHaveClass("btn-primary");

        const toggleInstructor = screen.getByTestId(`${testId}-cell-row-0-col-toggle-instructor-button`);
        expect(toggleInstructor).toBeInTheDocument();
        expect(toggleInstructor).toHaveClass("btn-primary");
    });

    test("Toggle Admin button works", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <UsersTable users={usersFixtures.threeUsers} showToggleButtons={true} />
            </QueryClientProvider>
        );

        await screen.findByTestId(`${testId}-cell-row-0-col-githubId`)
        expect(screen.getByTestId(`${testId}-cell-row-0-col-githubId`)).toHaveTextContent("11111");
        expect(screen.getByTestId(`${testId}-cell-row-0-col-fullName`)).toHaveTextContent("Phill Conrad");
        const toggleAdmin = screen.getByTestId(`${testId}-cell-row-0-col-toggle-admin-button`);
        expect(toggleAdmin).toBeInTheDocument();

        fireEvent.click(toggleAdmin);

    });


    test("Toggle Instructor button works", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <UsersTable users={usersFixtures.threeUsers} showToggleButtons={true} />
            </QueryClientProvider>
        );

        await screen.findByTestId(`${testId}-cell-row-0-col-githubId`)
        expect(screen.getByTestId(`${testId}-cell-row-0-col-githubId`)).toHaveTextContent("11111");
        expect(screen.getByTestId(`${testId}-cell-row-0-col-fullName`)).toHaveTextContent("Phill Conrad");
        const toggleInstructor = screen.getByTestId(`${testId}-cell-row-0-col-toggle-instructor-button`);
        expect(toggleInstructor).toBeInTheDocument();

        fireEvent.click(toggleInstructor);
    });
});
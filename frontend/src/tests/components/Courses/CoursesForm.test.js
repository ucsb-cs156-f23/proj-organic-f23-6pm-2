import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { BrowserRouter as Router } from "react-router-dom";

import CoursesForm from "main/components/Courses/CoursesForm";
import { coursesFixtures } from "fixtures/coursesFixtures";

import { QueryClient, QueryClientProvider } from "react-query";

const mockedNavigate = jest.fn();

jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useNavigate: () => mockedNavigate
}));

describe("CoursesForm tests", () => {
    const queryClient = new QueryClient();

    const expectedHeaders = ["Name", "School", "Term", "Start Date (iso format)", "End Date (iso format)", "GitHub Org"];
    const testId = "CoursesForm";

    test("renders correctly", async () => {

        render(
            <Router  >
                <CoursesForm />
            </Router>
        );
        await screen.findByText(/Name/);
        await screen.findByText(/Create/);
    });

    test("renders correctly when passing in a Course", async () => {

        render(
            <Router  >
                <CoursesForm initialContents={coursesFixtures.oneCourse} />
            </Router>
        );
        await screen.findByTestId(/CoursesForm-id/);
        expect(screen.getByText(/Id/)).toBeInTheDocument();
        expect(screen.getByTestId(/CoursesForm-id/)).toHaveValue("1");
    });

    test("Correct Error messsages on missing input", async () => {

        render(
            <Router  >
                <CoursesForm />
            </Router>
        );
        await screen.findByTestId("CoursesForm-submit");
        const submitButton = screen.getByTestId("CoursesForm-submit");

        fireEvent.click(submitButton);

        await screen.findByText(/Name is required./);
        expect(screen.getByText(/School is required./)).toBeInTheDocument();
        expect(screen.getByText(/Term is required./)).toBeInTheDocument();
        expect(screen.getByText(/Start Date is required./)).toBeInTheDocument();
        expect(screen.getByText(/End Date is required./)).toBeInTheDocument();
        expect(screen.getByText(/GitHub Org is required./)).toBeInTheDocument();

    });

    test("No Error messsages on good input", async () => {

        const mockSubmitAction = jest.fn();


        render(
            <Router  >
                <CoursesForm submitAction={mockSubmitAction} />
            </Router>
        );
        await screen.findByTestId("CoursesForm-name");

        const nameField = screen.getByTestId("CoursesForm-name");
        const schoolField = screen.getByTestId("CoursesForm-school");
        const termField = screen.getByTestId("CoursesForm-term");
        const startField = screen.getByTestId("CoursesForm-start");
        const endField = screen.getByTestId("CoursesForm-end");
        const githubOrgField = screen.getByTestId("CoursesForm-githubOrg");
        const submitButton = screen.getByTestId("CoursesForm-submit");

        fireEvent.change(nameField, { target: { value: 'course1' } });
        fireEvent.change(schoolField, { target: { value: 'school1' } });
        fireEvent.change(termField, { target: { value: 'term1' } });
        fireEvent.change(startField, { target: { value: '2023-01-01T12:00:00' } });
        fireEvent.change(endField, { target: { value: '2023-05-05T12:00:00' } });
        fireEvent.change(githubOrgField, { target: { value: 'org1' } });
        fireEvent.click(submitButton);

        await waitFor(() => expect(mockSubmitAction).toHaveBeenCalled());

        expect(screen.queryByText(/start must be in ISO format/)).not.toBeInTheDocument();
        expect(screen.queryByText(/end must be in ISO format/)).not.toBeInTheDocument();

    });

    test("that navigate(-1) is called when Cancel is clicked 1", async () => {

        render(
            <Router  >
                <CoursesForm />
            </Router>
        );
        await screen.findByTestId("CoursesForm-cancel");
        const cancelButton = screen.getByTestId("CoursesForm-cancel");

        fireEvent.click(cancelButton);

        await waitFor(() => expect(mockedNavigate).toHaveBeenCalledWith(-1));

    });

    test("renders correctly with no initialContents", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <Router>
                    <CoursesForm />
                </Router>
            </QueryClientProvider>
        );

        expect(await screen.findByText(/Create/)).toBeInTheDocument();

        expectedHeaders.forEach((headerText) => {
            const header = screen.getByText(headerText);
            expect(header).toBeInTheDocument();
        });

    });

    test("renders correctly when passing in initialContents", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <Router>
                    <CoursesForm initialContents={coursesFixtures.oneCourse} />
                </Router>
            </QueryClientProvider>
        );

        expect(await screen.findByText(/Create/)).toBeInTheDocument();

        expectedHeaders.forEach((headerText) => {
            const header = screen.getByText(headerText);
            expect(header).toBeInTheDocument();
        });

        expect(await screen.findByTestId(`${testId}-id`)).toBeInTheDocument();
        expect(screen.getByText(`Id`)).toBeInTheDocument();
    });

    test("that navigate(-1) is called when Cancel is clicked 2", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <Router>
                    <CoursesForm />
                </Router>
            </QueryClientProvider>
        );
        expect(await screen.findByTestId(`${testId}-cancel`)).toBeInTheDocument();
        const cancelButton = screen.getByTestId(`${testId}-cancel`);

        fireEvent.click(cancelButton);

        await waitFor(() => expect(mockedNavigate).toHaveBeenCalledWith(-1));
    });

    test("that the correct validations are performed", async () => {
        render(
            <QueryClientProvider client={queryClient}>
                <Router>
                    <CoursesForm />
                </Router>
            </QueryClientProvider>
        );

        expect(await screen.findByText(/Create/)).toBeInTheDocument();
        const submitButton = screen.getByText(/Create/);
        fireEvent.click(submitButton);

        await screen.findByText(/Name is required./);
        expect(screen.getByText(/School is required./)).toBeInTheDocument();
        expect(screen.getByText(/Term is required./)).toBeInTheDocument();
        expect(screen.getByText(/Start Date is required./)).toBeInTheDocument();
        expect(screen.getByText(/End Date is required./)).toBeInTheDocument();
        expect(screen.getByText(/GitHub Org is required./)).toBeInTheDocument();
    });

});
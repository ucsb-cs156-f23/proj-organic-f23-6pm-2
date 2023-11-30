import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import CoursesCreatePage from "main/pages/Courses/CoursesCreatePage";
import { QueryClient, QueryClientProvider } from "react-query";
import { MemoryRouter } from "react-router-dom";

import { apiCurrentUserFixtures } from "fixtures/currentUserFixtures";
import { systemInfoFixtures } from "fixtures/systemInfoFixtures";

import axios from "axios";
import AxiosMockAdapter from "axios-mock-adapter";

const mockToast = jest.fn();
jest.mock('react-toastify', () => {
    const originalModule = jest.requireActual('react-toastify');
    return {
        __esModule: true,
        ...originalModule,
        toast: (x) => mockToast(x)
    };
});

const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => {
    const originalModule = jest.requireActual('react-router-dom');
    return {
        __esModule: true,
        ...originalModule,
        Navigate: (x) => { mockNavigate(x); return null; }
    };
});

describe("CoursesCreatePage tests", () => {

    const axiosMock = new AxiosMockAdapter(axios);

    beforeEach(() => {
        jest.clearAllMocks();
        axiosMock.reset();
        axiosMock.resetHistory();
        axiosMock.onGet("/api/currentUser").reply(200, apiCurrentUserFixtures.userOnly);
        axiosMock.onGet("/api/systemInfo").reply(200, systemInfoFixtures.showingNeither);
    });

    test("renders without crashing", () => {
        const queryClient = new QueryClient();
        render(
            <QueryClientProvider client={queryClient}>
                <MemoryRouter>
                    <CoursesCreatePage />
                </MemoryRouter>
            </QueryClientProvider>
        );
    });

    test("on submit, makes request to backend, and redirects to /courses", async () => {

        const queryClient = new QueryClient();
        const course = {
            id: 3,
            name: "course3",
            school: "school3",
            term: "term3",
            start: "2023-03-03T12:00:00",
            end: "2023-07-07T12:00:00",
            githubOrg: "org3"
        };

        axiosMock.onPost("/api/courses/post").reply(202, course);

        render(
            <QueryClientProvider client={queryClient}>
                <MemoryRouter>
                    <CoursesCreatePage />
                </MemoryRouter>
            </QueryClientProvider>
        );

        await waitFor(() => {
            expect(screen.getByTestId("CoursesForm-name")).toBeInTheDocument();
        });

        const nameField = screen.getByTestId("CoursesForm-name");
        const schoolField = screen.getByTestId("CoursesForm-school");
        const termField = screen.getByTestId("CoursesForm-term");
        const startField = screen.getByTestId("CoursesForm-start");
        const endField = screen.getByTestId("CoursesForm-end");
        const githubOrgField = screen.getByTestId("CoursesForm-githubOrg");
        const submitButton = screen.getByTestId("CoursesForm-submit");

        fireEvent.change(nameField, { target: { value: 'course3' } });
        fireEvent.change(schoolField, { target: { value: 'school3' } });
        fireEvent.change(termField, { target: { value: 'term3' } });
        fireEvent.change(startField, { target: { value: '2023-03-03T12:00:00' } });
        fireEvent.change(endField, { target: { value: '2023-07-07T12:00:00' } });
        fireEvent.change(githubOrgField, { target: { value: 'org3' } });

        expect(submitButton).toBeInTheDocument();

        fireEvent.click(submitButton);

        await waitFor(() => expect(axiosMock.history.post.length).toBe(1));

        expect(axiosMock.history.post[0].params).toEqual({
            name: "course3",
            school: "school3",
            term: "term3",
            start: "2023-03-03T12:00",
            end: "2023-07-07T12:00",
            githubOrg: "org3"
        });

        // assert - check that the toast was called with the expected message
        expect(mockToast).toBeCalledWith("New Course Created - id: 3 name: course3");
        expect(mockNavigate).toBeCalledWith({ "to": "/courses" });

    });
});



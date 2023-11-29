import { fireEvent, render, waitFor, screen } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "react-query";
import { MemoryRouter } from "react-router-dom";
import CoursesEditPage from "main/pages/CoursesEditPage";

import { apiCurrentUserFixtures } from "fixtures/currentUserFixtures";
import { systemInfoFixtures } from "fixtures/systemInfoFixtures";
import axios from "axios";
import AxiosMockAdapter from "axios-mock-adapter";
import mockConsole from "jest-mock-console";

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
        useParams: () => ({
            id: 17
        }),
        Navigate: (x) => { mockNavigate(x); return null; }
    };
});

describe("CoursesEditPage tests", () => {

    describe("when the backend doesn't return data", () => {

        const axiosMock = new AxiosMockAdapter(axios);

        beforeEach(() => {
            axiosMock.reset();
            axiosMock.resetHistory();
            axiosMock.onGet("/api/currentUser").reply(200, apiCurrentUserFixtures.userOnly);
            axiosMock.onGet("/api/systemInfo").reply(200, systemInfoFixtures.showingNeither);
            axiosMock.onGet("/api/courses", { params: { id: 17 } }).timeout();
        });

        const queryClient = new QueryClient();
        test("renders header but table is not present", async () => {

            const restoreConsole = mockConsole();

            render(
                <QueryClientProvider client={queryClient}>
                    <MemoryRouter>
                        <CoursesEditPage />
                    </MemoryRouter>
                </QueryClientProvider>
            );
            await screen.findByText("Edit Courses");
            expect(screen.queryByTestId("Course-name")).not.toBeInTheDocument();
            restoreConsole();
        });
    });

    describe("tests where backend is working normally", () => {

        const axiosMock = new AxiosMockAdapter(axios);

        beforeEach(() => {
            axiosMock.reset();
            axiosMock.resetHistory();
            axiosMock.onGet("/api/currentUser").reply(200, apiCurrentUserFixtures.userOnly);
            axiosMock.onGet("/api/systemInfo").reply(200, systemInfoFixtures.showingNeither);
            axiosMock.onGet("/api/courses", { params: { id: 1 } }).reply(200, {
                id: 1,
                name: "CS156",
                school: "UCSB",
                term: "F23",
                start: "2023-09-01T00:00:00",
                end: "2023-12-31T00:00:00",
                githubOrg: "ucsb-cs156-f23"
            });
            axiosMock.onPut('/api/courses').reply(200, {
                id: "1",
                name: "CS148",
                school: "UCSB",
                term: "S24",
                start: "2024-01-01T00:00:00",
                end: "2024-03-31T00:00:00",
                githubOrg: "ucsb-cs148-w24"
            });
        });

        const queryClient = new QueryClient();
    
        test("Is populated with the data provided", async () => {

            render(
                <QueryClientProvider client={queryClient}>
                    <MemoryRouter>
                        <CoursesEditPage />
                    </MemoryRouter>
                </QueryClientProvider>
            );

            await screen.findByTestId("CoursesForm-id");

            const idField = screen.getByTestId("CoursesForm-id");
            const nameField = screen.getByTestId("CoursesForm-name");
            const schoolField = screen.getByTestId("CoursesForm-school");
            const termField = screen.getByTestId("CoursesForm-term");
            const startField = screen.getByTestId("CoursesForm-start");
            const endField = screen.getByTestId("CoursesForm-end");
            const githubOrgField = screen.getByTestId("CoursesForm-githubOrg");
            const submitButton = screen.getByTestId("CoursesForm-submit");

            expect(idField).toBeInTheDocument();
            expect(idField).toHaveValue("1");
            expect(nameField).toBeInTheDocument();
            expect(nameField).toHaveValue("CS156");
            expect(schoolField).toBeInTheDocument();
            expect(schoolField).toHaveValue("UCSB");
            expect(termField).toBeInTheDocument();
            expect(termField).toHaveValue("F23");
            expect(startField).toBeInTheDocument();
            expect(startField).toHaveValue("2023-09-01T00:00:00");
            expect(endField).toBeInTheDocument();
            expect(endField).toHaveValue("2023-12-31T00:00:00");
            expect(githubOrgField).toBeInTheDocument();
            expect(githubOrgField).toHaveValue("ucsb-cs156-f23");

            expect(submitButton).toHaveTextContent("Update");

            fireEvent.change(nameField, { target: { value: 'CS148' } });
            fireEvent.change(schoolField, { target: { value: 'UCSB' } });
            fireEvent.change(termField, { target: { value: 'S24' } });
            fireEvent.change(startField, { target: { value: '2024-01-01T00:00:00' } });
            fireEvent.change(endField, { target: { value: '2024-03-31T00:00:00' } });
            fireEvent.change(githubOrgField, { target: { value: 'ucsb-cs148-w24' } });
            fireEvent.click(submitButton);

            await waitFor(() => expect(mockToast).toBeCalled());
            expect(mockToast).toBeCalledWith("Course Updated - id: 1 name: CS148 school: UCSB term: S24 start: 2024-01-01T00:00:00 end: 2024-03-31T00:00:00 githubOrg: ucsb-cs148-w24");
            
            expect(mockNavigate).toBeCalledWith({ "to": "/courses" });

            expect(axiosMock.history.put.length).toBe(1); // times called
            expect(axiosMock.history.put[0].params).toEqual({ id: 1 });
            expect(axiosMock.history.put[0].data).toBe(JSON.stringify({
                name: "CS148",
                school: "UCSB",
                term: "S24",
                start: "2024-01-01T00:00:00",
                end: "2024-03-31T00:00:00",
                githubOrg: "ucsb-cs148-w24"
            })); // posted object


        });

        test("Changes when you click Update", async () => {

            render(
                <QueryClientProvider client={queryClient}>
                    <MemoryRouter>
                        <CoursesEditPage />
                    </MemoryRouter>
                </QueryClientProvider>
            );

            await screen.findByTestId("CoursesForm-id");

            const idField = screen.getByTestId("CoursesForm-id");
            const nameField = screen.getByTestId("CoursesForm-name");
            const schoolField = screen.getByTestId("CoursesForm-school");
            const termField = screen.getByTestId("CoursesForm-term");
            const startField = screen.getByTestId("CoursesForm-start");
            const endField = screen.getByTestId("CoursesForm-end");
            const githubOrgField = screen.getByTestId("CoursesForm-githubOrg");
            const submitButton = screen.getByTestId("CoursesForm-submit");

            expect(idField).toBeInTheDocument();
            expect(idField).toHaveValue("1");
            expect(nameField).toBeInTheDocument();
            expect(nameField).toHaveValue("CS156");
            expect(schoolField).toBeInTheDocument();
            expect(schoolField).toHaveValue("UCSB");
            expect(termField).toBeInTheDocument();
            expect(termField).toHaveValue("F23");
            expect(startField).toBeInTheDocument();
            expect(startField).toHaveValue("2023-09-01T00:00:00");
            expect(endField).toBeInTheDocument();
            expect(endField).toHaveValue("2023-12-31T00:00:00");
            expect(githubOrgField).toBeInTheDocument();
            expect(githubOrgField).toHaveValue("ucsb-cs156-f23");

            fireEvent.change(nameField, { target: { value: 'CS148' } });
            fireEvent.change(schoolField, { target: { value: 'UCSB' } });
            fireEvent.change(termField, { target: { value: 'S24' } });
            fireEvent.change(startField, { target: { value: '2024-01-01T00:00:00' } });
            fireEvent.change(endField, { target: { value: '2024-03-31T00:00:00' } });
            fireEvent.change(githubOrgField, { target: { value: 'ucsb-cs148-w24' } });

            fireEvent.click(submitButton);

            await waitFor(() => expect(mockToast).toBeCalled());
            expect(mockToast).toBeCalledWith("Course Updated - id: 1 name: CS148 school: UCSB term: S24 start: 2024-01-01T00:00:00 end: 2024-03-31T00:00:00 githubOrg: ucsb-cs148-w24");
            expect(mockNavigate).toBeCalledWith({ "to": "/courses" });
        });

       
    });
});
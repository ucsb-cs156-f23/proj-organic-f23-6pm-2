import { Button, Form } from 'react-bootstrap';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';

function CoursesForm({ initialContents, submitAction, buttonLabel = "Create" }) {
    
    // Stryker disable all
    const {
        register,
        formState: { errors },
        handleSubmit,
    } = useForm(
        { defaultValues: initialContents || {}, }
    );
    // Stryker restore all
   
    const navigate = useNavigate();

    // For explanation, see: https://stackoverflow.com/questions/3143070/javascript-regex-iso-datetime
    // Note that even this complex regex may still need some tweaks

    // Stryker disable next-line Regex
    const isodate_regex = /(\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d\.\d+)|(\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d)|(\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d)/i;

    const testIdPrefix = "CoursesForm";

    return (
        <Form onSubmit={handleSubmit(submitAction)}>

            {initialContents && (
                <Form.Group className="mb-3" >
                    <Form.Label htmlFor="id">Id</Form.Label>
                    <Form.Control
                        data-testid={testIdPrefix + "-id"}
                        id="id"
                        type="text"
                        {...register("id")}
                        value={initialContents.id}
                        disabled
                    />
                </Form.Group>
            )}

            <Form.Group className="mb-3" >
                <Form.Label htmlFor="name">Name</Form.Label>
                <Form.Control
                    data-testid={testIdPrefix + "-name"}
                    id="name"
                    type="text"
                    isInvalid={Boolean(errors.name)}
                    {...register("name", {
                        required: "Name is required."
                    })}
                />
                <Form.Control.Feedback type="invalid">
                    {errors.name?.message}
                </Form.Control.Feedback>
            </Form.Group>

            <Form.Group className="mb-3" >
                <Form.Label htmlFor="school">School</Form.Label>
                <Form.Control
                    data-testid={testIdPrefix + "-school"}
                    id="school"
                    type="text"
                    isInvalid={Boolean(errors.school)}
                    {...register("school", {
                        required: "School is required."
                    })}
                />
                <Form.Control.Feedback type="invalid">
                    {errors.school?.message}
                </Form.Control.Feedback>
            </Form.Group>

            <Form.Group className="mb-3" >
                <Form.Label htmlFor="term">Term</Form.Label>
                <Form.Control
                    data-testid={testIdPrefix + "-term"}
                    id="term"
                    type="text"
                    isInvalid={Boolean(errors.term)}
                    {...register("term", {
                        required: "Term is required."
                    })}
                />
                <Form.Control.Feedback type="invalid">
                    {errors.term?.message}
                </Form.Control.Feedback>
            </Form.Group>

            <Form.Group className="mb-3" >
                <Form.Label htmlFor="startDate">Start Date (iso format)</Form.Label>
                <Form.Control
                    data-testid={testIdPrefix + "-startDate"}
                    id="startDate"
                    type="datetime-local"
                    isInvalid={Boolean(errors.startDate)}
                    {...register("startDate", { required: true, pattern: isodate_regex })}
                />
                <Form.Control.Feedback type="invalid">
                    {errors.startDate && 'Start Date is required.'}
                </Form.Control.Feedback>
            </Form.Group>

            <Form.Group className="mb-3" >
                <Form.Label htmlFor="endDate">End Date (iso format)</Form.Label>
                <Form.Control
                    data-testid={testIdPrefix + "-endDate"}
                    id="endDate"
                    type="datetime-local"
                    isInvalid={Boolean(errors.endDate)}
                    {...register("endDate", { required: true, pattern: isodate_regex })}
                />
                <Form.Control.Feedback type="invalid">
                    {errors.endDate && 'End Date is required.'}
                </Form.Control.Feedback>
            </Form.Group>

            <Form.Group className="mb-3" >
                <Form.Label htmlFor="githubOrg">GitHub Org</Form.Label>
                <Form.Control
                    data-testid={testIdPrefix + "-githubOrg"}
                    id="githubOrg"
                    type="text"
                    isInvalid={Boolean(errors.githubOrg)}
                    {...register("githubOrg", {
                        required: "GitHub Org is required."
                    })}
                />
                <Form.Control.Feedback type="invalid">
                    {errors.githubOrg?.message}
                </Form.Control.Feedback>
            </Form.Group>


            <Button
                type="submit"
                data-testid={testIdPrefix + "-submit"}
            >
                {buttonLabel}
            </Button>
            <Button
                variant="Secondary"
                onClick={() => navigate(-1)}
                data-testid={testIdPrefix + "-cancel"}
            >
                Cancel
            </Button>

        </Form>

    )
}

export default CoursesForm;
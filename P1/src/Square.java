/**
 * The squares that form the production grid, they contain the ids of vaccineDoses that reside in them or 0 if none,
 * and the presentRobot that resides in that square
 */
public class Square {
    private RobotWorker presentRobot;
    private int vaccineDose; // vaccine dose ID

    public Square(RobotWorker presentRobot, int vaccineDose) {
        this.presentRobot = presentRobot;
        this.vaccineDose = vaccineDose;
    }

    public Square() {
        presentRobot = null;
        vaccineDose = 0;
    }

    public RobotWorker getPresentRobot() {
        return presentRobot;
    }

    public void setPresentRobot(RobotWorker presentRobot) {
        this.presentRobot = presentRobot;
    }

    public int getVaccineDose() {
        return vaccineDose;
    }

    public void setVaccineDose(int vaccineDose) {
        this.vaccineDose = vaccineDose;
    }

    public int pollVaccine() {
        int result = vaccineDose;
        vaccineDose=0;
        return result;
    }
}

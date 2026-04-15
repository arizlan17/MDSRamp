package Task04.dto;

public class CustomerDeptDto {

        private String customerName;
        private String deptName;

    public CustomerDeptDto(String customerName, String deptName) {
        this.customerName = customerName;
        this.deptName = deptName;
    }

        @Override
        public String toString() {
            return "Join Data [Name: " + customerName + " | Department: " + deptName + "]";
        }

        // Getters
        public String getCustomerName() { return customerName; }
        public String getDeptName() { return deptName; }

}

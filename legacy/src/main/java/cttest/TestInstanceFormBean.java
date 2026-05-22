package cttest;

import static constants.Constants.PAGINATION_SIZE;
import static constants.Constants.XLSX;
import static constants.Constants.dd_MM_yyyy;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.CollectionUtils;

import bl.StudentTestBL;
import bl.TestBL;
import bl.TestInstanceBL;
import lombok.Getter;
import lombok.Setter;
import model.Question;
import model.StudentTest;
import model.Test;
import model.TestInstance;
import model.enums.StudentTestStatus;
import model.enums.TestInstanceStatus;
import model.enums.noper.StudentTestSortType;
import model.noper.GenericPagination;
import model.noper.StringPair;
import util.FileDownloadUtil;
import util.ResUtil;
import util.SpringContextUtil;

@Getter @Setter
@ManagedBean
@ViewScoped
public class TestInstanceFormBean extends BaseManagedBean implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static final Logger logger = LogManager.getLogger(TestInstanceFormBean.class);
    
    private static final String RESULTS_TAB_ID = "TIReTbId";
    
    protected TestInstanceBL serviceTestInstanceBL = 
			new SpringContextUtil<TestInstanceBL>()
			.initializeBean(TestInstanceBL.class);
    
    protected TestBL serviceTestBL = 
			new SpringContextUtil<TestBL>()
			.initializeBean(TestBL.class);
    
    protected StudentTestBL serviceStudentTestBL = 
			new SpringContextUtil<StudentTestBL>()
			.initializeBean(StudentTestBL.class);
    
    private TestInstance currentObject;
    private String currentTab;
    
    private Test currentObjectTest;
    
    private GenericPagination<StudentTest> pagination;
    
    private List<StringPair> statusComboList;
    private List<StringPair> sortTypeComboList;
    
    private String filterName;
    private StudentTestStatus filterStatus;
    private StudentTestSortType sortType = StudentTestSortType.HIGHEST_SCORE;
    
    // TODO DTO??
    /**
     * Resultados de test
     * 
     * Clave: ID pregunta
     * Valor: ID respuesta correcta
     */
    private Map<Integer, Integer> testResults;
    
    private StudentTest currentStudentTest;
    
    
	@PostConstruct
    public void init() {
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
	    String idParam = facesContext.getExternalContext().getRequestParameterMap().get("id");
	    Integer id = (idParam != null) ? Integer.valueOf(idParam) : null;
	    
//	    if (mode != null) {
//	    	if (mode.equals("new")) {
//	    		initCurrentObject();
//	    	}
//	    }
	    
	    if (id != null) {
	    	
	    	 try {
	    		
	    		 _load(id);
	    		 _loadTest(currentObject.getTestId());
	    		 searchStudentTestList();
	    		 _loadTestResults();
	    		 
 	        } catch (Exception e) {
 	          
 	        }
	    	
	    }
	    
	    currentTab = RESULTS_TAB_ID;
	    
    }
	
	private void _load(Integer id) {
		
		try {
			
			currentObject = serviceTestInstanceBL.read(id);
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(),
							e.getMessage()));
			
		}
		
	}
	
	private void _loadTest(Integer testId) {
		
		try {
			
			currentObjectTest = serviceTestBL.read(testId);
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(),
							e.getMessage()));
			
		}
		
	}
	
	public void searchStudentTestList() {
	    	
    	try {
    		
    		List<StudentTest> list = serviceStudentTestBL
    				.finder(currentObject.getId(), filterName,
    						filterStatus, sortType);
    		
    		pagination = new GenericPagination<StudentTest>(list, PAGINATION_SIZE);
    		
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(),
							e.getMessage()));
			
		}
    	
    }
	
	private void _loadTestResults() {
		
		if (currentObjectTest == null
				|| CollectionUtils.isEmpty(currentObjectTest.getQuestions())) {
			return;
		}
		
		testResults = new LinkedHashMap<>();
		
		for (Question q : currentObjectTest.getQuestions()) {
			
			Integer correctAnswerId = q.getCorrectAnswerId();
			
			if (correctAnswerId != null) {
				testResults.put(q.getId(), correctAnswerId);
			}
			
		}
		
	}
	
    public void exportResultsToExcel() {
    	
    	try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
    		
    		Workbook workbook = new XSSFWorkbook();
	        Sheet sheet = workbook.createSheet(ResUtil.get("label_results"));
	        
	        int nRow = 0;
	        
	        _createExcelHeaders(sheet, nRow++);
	        
	        _createExcelRows(sheet, nRow++);
	        
	        for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
	            sheet.autoSizeColumn(i);
	        }
        
            workbook.write(out);
            workbook.close();
            
            String fileName = currentObjectTest.getName() + " - " + currentObject.getPin();
            
            FileDownloadUtil.downloadFile(fileName + XLSX, out.toByteArray());

            FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, 
							FacesMessage.SEVERITY_INFO.toString(),
							ResUtil.get("label_excel_file_exported_successfully")));
            
        } catch (Exception e) {
        	
        	StackTraceElement element = e.getStackTrace()[0];
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
	                    element.getClassName(), element.getMethodName(), 
	                    e.getClass().getName(), e.getMessage());
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(),
							ResUtil.get("label_error_trying_to_export_results")));
            
        }
        
    }
	
    private void _createExcelHeaders(Sheet sheet, int nRow) {
    	
    	String[] headers = {ResUtil.get("label_student_id"),
        		ResUtil.get("label_responsible"),
        		ResUtil.get("label_center"),
        		ResUtil.get("label_birth_date"),
        		ResUtil.get("label_execution_date"),
        		ResUtil.get("label_gender"),
				ResUtil.get("label_comment")};
    	
        Row headerRow = sheet.createRow(nRow);
        int nCol = 0;
        
        for (int i = 0; i < headers.length - 1; i++) {
        	headerRow.createCell(nCol++).setCellValue(headers[i]);
        }
        
        if (!CollectionUtils.isEmpty(currentObjectTest.getQuestions())) {
        	 for (int i = 0; i < currentObjectTest.getQuestions().size(); i++) {
                 headerRow.createCell(nCol++).setCellValue("P" + (i + 1));
             }
        }
        
        headerRow.createCell(nCol++).setCellValue(headers[headers.length-1]);
        
    }
    
    private void _createExcelRows(Sheet sheet, int nRow) {
    	
    	if (pagination == null
    			|| CollectionUtils.isEmpty(pagination.getObjectList())) {
    		return;
    	}
    	
    	for (StudentTest st : pagination.getObjectList()) {
    		
    		Row row = sheet.createRow(nRow++);
    		int nCol = 0;
    		
    		row.createCell(nCol++).setCellValue(st
    				.getStudent().getIdentifier());
    		
    		row.createCell(nCol++).setCellValue(
    				currentObject.getAssignedBy().getUsername());
    		
    		row.createCell(nCol++).setCellValue(
    				currentObject.getStudentGroup().getCenter().getName());
    		
    		row.createCell(nCol++).setCellValue(st
    				.getStudent().getBirthDate() != null
    						? st.getStudent().getBirthDate()
    								.format(DateTimeFormatter.ofPattern(dd_MM_yyyy))
    	    				: "");
    		
    		row.createCell(nCol++).setCellValue(currentObject.getAssignedDate()
    				.format(DateTimeFormatter.ofPattern(dd_MM_yyyy)));
    		
    		row.createCell(nCol++).setCellValue(st
    				.getStudent().getGender().getTitle());
    		
    		
			for (Question q : currentObjectTest.getQuestions()) {
				
				Integer studentAnswerId = st.getAnswerId(q.getId());
				
				if (studentAnswerId == null) {
					row.createCell(nCol++).setCellValue("");
					continue;
				}
				
				for (int i = 0; i < currentObjectTest.getQuestions().size(); i++) {
					if (q.getAnswers().get(i).getId().equals(studentAnswerId)) {
						row.createCell(nCol++).setCellValue(i+1);
						break;
					}
				}
				
			}
			
    		row.createCell(nCol++).setCellValue(st.getComment());
    		
    	}
    	
    }
 
    public void saveCurrentStudentTest() {
    	
    	if (currentStudentTest == null) {
    		return;
    	}
    	
    	try {
			
			serviceStudentTestBL.update(currentStudentTest, super.getLoggedUserId());
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, 
							FacesMessage.SEVERITY_INFO.toString(),
							ResUtil.get("label_record_updated")));
			
		} catch (Exception e) {
			
			StackTraceElement element = e.getStackTrace()[0];
			
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, 
							FacesMessage.SEVERITY_ERROR.toString(),
							e.getMessage()));
			
    	}
    	
    }
    
    public void closeTestInstance() {
    	
    	try {
    		
    		serviceTestInstanceBL.closeTestInstance(
    				currentObject.getId(), super.getLoggedUserId());
    		
    		currentObject.setStatus(TestInstanceStatus.CLOSED);
    		
    		searchStudentTestList();
    		
    		FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, 
							FacesMessage.SEVERITY_INFO.toString(),
							ResUtil.get("label_test_closed")));
    		
    	} catch (Exception e) {
    		
    		StackTraceElement element = e.getStackTrace()[0];
			logger.error("Exception in Class: {}, Method: {}, Type: {}, Message: {}",
					element.getClassName(), element.getMethodName(),
					e.getClass().getName(), e.getMessage());
			
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							FacesMessage.SEVERITY_ERROR.toString(),
							e.getMessage()));
			
    	}
    	
    }
    
    
	public void calculateScore() {
	
//		try {
//			
//			serviceStudentTestBL.calculateScore();
//			
//		} catch (Exception e) {
//			
//			
//		}
		
	}
    
	public List<StringPair> getStatusComboList() {
		if (statusComboList == null) {
			statusComboList = StudentTestStatus.getComboList();
		}
		return statusComboList;
	}
	
	public List<StringPair> getSortTypeComboList() {
		if (sortTypeComboList == null) {
			sortTypeComboList = StudentTestSortType.getComboList();
		}
		return sortTypeComboList;
	}
	
	public void clearFilters() {
		filterStatus = null;
	}
	
	public Integer getFilterStatusId() {
		return filterStatus != null ? filterStatus.getId() : null;
	}
	
	public void setFilterStatusId(Integer id) {
		filterStatus = StudentTestStatus.getById(id);
	}
	
	public Integer getSortTypeId() {
		return sortType != null ? sortType.getId() : null;
	}
	
	public void setSortTypeId(Integer id) {
		sortType = StudentTestSortType.getById(id);
	}

	public String getResultsTabId() {
		return RESULTS_TAB_ID;
	}
	
	public String getStudentTestResumeByStatus() {
		
		if (pagination == null
				 || CollectionUtils.isEmpty(pagination.getObjectList())) {
	        return "[]";
	    }
		
	    Map<StudentTestStatus, Long> counts = pagination.getObjectList().stream()
		        .filter(x -> x.getStatus() != null)
		        .collect(Collectors.groupingBy(
		        		StudentTest::getStatus, Collectors.counting()));
	    
	    Integer nStudentTests = pagination.getObjectList().size();

	    String values = counts.entrySet().stream()
	    		.map(x -> "['" + x.getKey().getTitle() + "', " + 
    					new BigDecimal(x.getValue())
    							.multiply(new BigDecimal(100))
					            .divide(new BigDecimal(nStudentTests),
					            		2, RoundingMode.HALF_UP)
					            .toString() + "]")
    		    .collect(Collectors.joining(","));
	    
	    return "[" +  "['" + ResUtil.get("label_status")
				+ "', '" + ResUtil.get("label_percentage") + "']" + ","
				+ values + "]";
	    
	}
	
	public String getScoresAsString() {
		
	    if (pagination == null
	    		|| CollectionUtils.isEmpty(pagination.getObjectList())) {
	        return "[]";
	    }

	    String values = pagination.getObjectList().stream()
		        .map(StudentTest::getScore)
		        .filter(Objects::nonNull)
		        .map(String::valueOf)
		        .collect(Collectors.joining(","));

	    return "[" + values + "]";
	    
	}


//	public String getChartDataJson() {
//	    List<List<Object>> chartData = getChartData().stream()
//	        .map(pair -> Arrays.asList(pair.getLabel(), pair.getValue()))
//	        .collect(Collectors.toList());
//
//	    return new Gson().toJson(chartData);
//	}
	
}
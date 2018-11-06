package io.github.cloudiator.deployment.spark;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel Seybold on 20.09.2018.
 */
public class LivyBatch {

  public LivyBatch() {

  }

  /**
   * File containing the application to execute, path to HTTP or HDFS endpoint
   */
  private String file;

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }

  /**
   * User to impersonate when running the job
   */
  private String proxyUser;

  public String getProxyUser() {
    return proxyUser;
  }

  public void setProxyUser(String proxyUser) {
    this.proxyUser = proxyUser;
  }

  /**
   * Application Java/Spark main class
   */
  private String className;

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }


  /**
   * Command line arguments for the application
   */
  private List<String> args = new ArrayList<String>();

  public List<String> getArgs() {
    return args;
  }

  public void setArgs(List<String> args) {
    this.args = args;
  }




  /**
   * jars to be used in this batch session
   */
  private List<String> jars = new ArrayList<String>();

  public List<String> getJars() {
    return jars;
  }

  public void setJars(List<String> jars) {
    this.jars = jars;
  }

  /**
   * Python files to be used in this session
   */
  private List<String> pyFiles = new ArrayList<String>();

  public List<String> getPyFiles() {
    return pyFiles;
  }

  public void setPyFiles(List<String> pyFiles) {
    this.pyFiles = pyFiles;
  }

  /**
   * files to be used in this session
   */
  private List<String> files = new ArrayList<String>();

  public List<String> getFiles() {
    return files;
  }

  public void setFiles(List<String> files) {
    this.files = files;
  }

  /**
   * Amount of memory to use for the driver process
   */
  private String driverMemory;

  public String getDriverMemory() {
    return driverMemory;
  }

  public void setDriverMemory(String driverMemory) {
    this.driverMemory = driverMemory;
  }

  /**
   * Number of cores to use for the driver process
   */
  private int driverCores;

  public int getDriverCores() {
    return driverCores;
  }

  public void setDriverCores(int driverCores) {
    this.driverCores = driverCores;
  }

  /**
   * Amount of memory to use per executor process
   */
  private String executorMemory;

  public String getExecutorMemory() {
    return executorMemory;
  }

  public void setExecutorMemory(String executorMemory) {
    this.executorMemory = executorMemory;
  }

  /**
   * Number of cores to use for each executor
   */
  private int executorCores;

  public int getExecutorCores() {
    return executorCores;
  }

  public void setExecutorCores(int executorCores) {
    this.executorCores = executorCores;
  }

  /**
   * Number of executors to launch for this batch session
   */
  private int numExecutors;

  public int getNumExecutors() {
    return numExecutors;
  }

  public void setNumExecutors(int numExecutors) {
    this.numExecutors = numExecutors;
  }

  /**
   * Archives to be used in this session
   */
  private List<String> archives = new ArrayList<String>();

  public List<String> getArchives() {
    return archives;
  }

  public void setArchives(List<String> archives) {
    this.archives = archives;
  }

  /**
   * The name of the YARN queue to which submitted
   */
  private String queue;

  public String getQueue() {
    return queue;
  }

  public void setQueue(String queue) {
    this.queue = queue;
  }

  /**
   * The name of this batch session
   */
  private String name;


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * Spark configuration properties
   */
  private Map conf = new HashMap<String, String>();

  public Map getConf() {
    return conf;
  }

  public void setConf(Map conf) {
    this.conf = conf;
  }











}
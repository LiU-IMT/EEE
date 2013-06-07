You posted log data:<br/> <?php echo htmlspecialchars($_POST['message']); ?> at <?php echo date('Y-m-d H:i:s'); ?>
<hr/><?php  
$myFile = "studie-log.txt";
$fh = fopen($myFile, 'a') or die("can't open file");
$stringData = "----, " . $_SERVER['REMOTE_ADDR'] . ", " . date('Y-m-d') ." ";
// fwrite($fh, $stringData . "\n");
// fwrite($fh, $_POST['data']);
fwrite($fh,  date('H:i:s').', '.$_POST['timestamp'] .', '. $_POST['message'] .', '. $stringData . "\n");
// fwrite($fh, "Full post:" $_POST);
fclose($fh);
# autocomplete-packet-interceptor
This repository is intended to practically demonstrate vulnerabilities in Auto-complete search functionality on website (suggestion boxes in search fields).
Even though traffic is encrypted, search queries can still be retrieved - the code in this project demonstrates that search queries made on [Amazon.co.uk](https://www.amazon.co.uk/) can easily be decrypted through a side-channel attack. 
This project is based on the work of [Shuo Chen, Rui Wang, XiaoFeng Wang, Kehuan Zhang] (http://research.microsoft.com/pubs/119060/WebAppSideChannel-final.pdf) and the implementation uses their terminology. The code is intended as a framework for performing side-channel attacks and decrypting search queries, not just against Amazon, but could be extended to other websites. Furthermore, this project contains tools to evaluate the efficiancy of this methods.

Please note that this is only intended for demonstrational purposes and should not be used in any antagonistic way.


## How to run
There are two ways to perform a side-channel channel attack. Either by using the on-the-fly interceptor, or the precomputed-profile interceptor. In either case, Maven is required.

First, do a `mvn package`

### On-the-fly interceptor
To run the on-the-fly interceptor

`java -cp target/autocomplete-packet-interceptor-0.0.1-SNAPSHOT.jar com.alexsebbe.interceptor.OnTheFlyInterceptorRunner`

The program is going to ask you which interface to use for capturing traffic. Use the interface which transfers web traffic (usually an ethernet interface with an IP address set).
After initialization has completed, you can type a search query into the main search field on [Amazon.co.uk](https://www.amazon.co.uk/). Type slowly!

### Precomputed-profile interceptor
To use the precomputed-profile interceptor, a profile must first be generated. To generate a profile, use the StateCollector.

`java -cp target/autocomplete-packet-interceptor-0.0.1-SNAPSHOT.jar com.alexsebbe.interceptor.StateCollectorRunner depth thread_count`
 
where depth is the largest amount of characters you wish to decrypt, for example 5 would decrypt any search query 'aaaaa'-'zzzzz', and thread_count is the amount of threads which will send asynchronous requests (usually 60 is good). The profile will be stored in /mappings. Make sure that folder exists.
Beware: Profiles are stored in JSON format and may take a lot of space.  

After a profile has been generated, the profile can be used to ~instantly decrypt auto-complete packets, using the PrecomputedProfileInterceptorRunner

`java -cp target/autocomplete-packet-interceptor-0.0.1-SNAPSHOT.jar com.alexsebbe.interceptor.PrecomputedProfileInterceptorRunner path_to_profile`

### Statistics 
To show general statistics, such as #validInput, #distingushableInput, and average prediction rate, for a profile (generated as described above), use the StatisticsRunner

`java -cp target/autocomplete-packet-interceptor-0.0.1-SNAPSHOT.jar com.alexsebbe.statistics.StatisticsRunner path_to_profile depth_of_profile`

To show the degradation rate of a profile, compared to new profiles

`java -cp target/autocomplete-packet-interceptor-0.0.1-SNAPSHOT.jar com.alexsebbe.statistics.StatisticsDegradationRunner path_to_profile_original_profile path_to_new_profile path_to_new_profile ...`













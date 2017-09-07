TagRec
======

## Towards A Standardized Tag Recommender Benchmarking Framework

TagRec won the best poster award @ Hypertext 2014 (HT'14) conference: [http://ht.acm.org/ht2014/index.php?awards.poster](http://ht.acm.org/ht2014/index.php?awards.poster)

## Description
The aim of this work (please [cite](https://github.com/learning-layers/TagRec/#citation)) is to provide the community with a simple to use, generic tag-recommender framework written in Java to evaluate novel tag-recommender algorithms with a set of well-known std. IR metrics such as nDCG, MAP, MRR, Precision (P@k), Recall (R@k), F1-score (F1@k), Diversity (D), Serendipity (S), User Coverage (UC) and folksonomy datasets such as BibSonomy, CiteULike, LastFM, Flickr, MovieLens or Delicious and to benchmark the developed approaches against state-of-the-art tag-recommender algorithms such as MP, MP_r, MP_u, MP_u,r, CF, APR, FR, GIRP, GIRPTM, etc.

Furthermore, it contains algorithms to process datasets (e.g., p-core pruning, leave-one-out or 80/20 splitting, LDA topic creation and create input files for other recommender frameworks).

The software already contains four novel tag-recommender approaches based on cognitive science theory. The first one ([3Layers](http://www.christophtrattner.info/pubs/cikm2013.pdf)) (Seitlinger et al, 2013) uses topic information and is based on the ALCOVE/MINERVA2 theories (Krutschke, 1992; Hintzman, 1984). The second one ([BLL+C](http://delivery.acm.org/10.1145/2580000/2576934/p463-kowald.pdf)) (Kowald et al., 2014b) uses time information is based on the ACT-R theory (Anderson et al., 2004). The third one ([3LT](http://www.christophtrattner.info/pubs/msm8_kowald.pdf)) (Kowald et al., 2015b) is a combination of the former two approaches and integrates the time component on the level of tags and topics. Finally, the fourth one ([BLLac+MPr](http://www.christophtrattner.info/pubs/msm7_kowald.pdf)) extends the BLL+C algorithm with semantic correlations (Kowald et al., 2015a).

Apart from this, TagRec also contains algorithms for the personalized recommendation of resources / items in social tagging systems. In this respect TagRec includes a novel algorithm called [CIRTT](http://www.christophtrattner.info/pubs/sp2014.pdf) (Lacic et al., 2014) that integrates tag and time information using the BLL-equation coming from the ACT-R theory (Anderson et al, 2004). Furthermore, it contains another novel item-recommender called [SUSTAIN+CFu](http://arxiv.org/pdf/1501.07716v1.pdf) (Seitlinger et al., 2015) that improves user-based CF via integrating the addentional focus of users via the SUSTAIN model (Love et al., 2004).

Finally, TagRec was also utilized for the recommendation of hashtags in Twitter (Kowald et al., 2017). Thus, it contains an initial set of algorithms for this task as well. For this, TagRec also contains a connection to the Apache Solr search engine framework.

This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.

Please cite [the paper](https://github.com/learning-layers/TagRec/#citation) if you use this software in one of your publications.

## Download
The source-code can be directly checked-out through this repository. It contains an Eclipse project to edit and build it and an already deployed .jar file for direct execution. Furthermore, the folder structure that is provided in the repository is needed, where _csv_ is the input directory and _metrics_ is the output directory in the _data_ folder. Both of these directories contain subdirectories for the different datasets:
* bib_core for BibSonomy
* cul_core for CiteULike
* del_core for Delicious
* flickr_core for Flickr
* ml_core for MovieLens
* lastfm_core for LastFM
* wiki_core for Wikipedia (based on bookmarks from Delicious)
* twitter_core/researchers for the Twitter CompSci dataset
* twitter_core/general for the Twitter Random dataset

## How-to-use
The _tagrec_ .jar uses three parameters:

First the algorithm,

Tag-Recommender:
* 3layers for 3Layers (based on ALCOVE/MINERVA2 theories) (Seitlinger et al., 2013)
* 3LT for the time-based 3Layers on the levels of tags and topics (Kowald et al., 2014a)
* bll_c for BLL and BLL+C (based on ACT-R theory) (Kowald et al., 2014b)
* bll_c_ac for BLL and BLL+MPr together with semantic correlations (Trattner et al., 2014)
* lda for Latent Dirichlet Allocation (Krestel et al., 2009)
* cf for (user-based) Collaborative Filtering (Jäschke et al., 2007)
* cfr for (resource-based and mixed) Collaborative Filtering (Jäschke et al., 2007)
* fr for Adapted PageRank and FolkRank (Hotho et al., 2006)
* girptm for GIRP and GIRPTM (Zhang et al., 2012)
* mp for MostPopular tags (Jäschke et al., 2007)
* mp_u_r for MostPopular tags by user and/or resource (Jäschke et al., 2007)

Resource-Recommender:
* item_sustain for the improved CF approach based on the SUSTAIN model
* item_cirtt for the tag- and time-based approach based on BLL (Lacic et al., 2014)
* item_mp for MostPopular items
* item_cbt for the content-based Filtering approach using Topics
* item_cft for user-based Collaborative Filtering based on tag-profiles
* item_cfb for user-based Collaborative Filtering based on user-item matrix
* item_zheng for the tag- and time-based approach by Zheng et al. (2011)
* item_huang for the tag- and time-based appraoch by Huang et al. (2014)

Hashtag-Recommender (Kowald et al., 2017):
* hashtag_analysis for analyzing the temporal effects on hashtag reuse
* hashtag_socialmp for MostPopular hashtags of the followees (i.e., MPs)
* hashtag socialrecency for recency-ranked hashtags of the followees (i.e., MRs)
* hashtag_socialbll for BLL-ranked hashtags of the followees (i.e., BLLs)
* hashtag_hybrid for BLLi,s
* hashtag_cb_res for BLLi,s,c on the Twitter CompSci dataset and SOLR core
* hashtag_cb_gen for BLLi,s,c on the Twitter Random dataset and SOLR core

Data-Processing:
* stats for printing the dataset statistics
* core for calculating p-cores on a dataset
* split_l1o for splitting a dataset into training and test-sets using a leave-one-out method
* split_8020 for splitting a dataset into training and test-sets using 80/20 split
* lda_samples for creating LDA topics for the resources in a dataset
* tensor_samples for creating samples for the FM and PITF methods implemented in [PITF and FM algorithms](http://www.informatik.uni-konstanz.de/rendle/software/tag-recommender/)
* mymedialite_samples for creating samples for the WRMF method implemented in [MyMediaLite](http://www.mymedialite.net/)
* process_bibsonomy for converting the BibSonomy dataset into the TagRec format
* process_citeulike for converting the CiteUlike dataset into the TagRec format
* process_lastfm for converting the LastFM dataset into the TagRec format
* process_ml for converting the MovieLens dataset into the TagRec format
* process_del for converting the Delicious dataset into the TagRec format
* process_flickr for converting the Flickr dataset into the TagRec format

, second the dataset(-directory):
* bib for BibSonomy
* cul for CiteULike
* del for Delicious
* flickr for Flickr
* ml for MovieLens
* lastfm for LastFM
* wiki for Wikipedia (based on bookmarks from Delicious)
* twitter_res for the Twitter CompSci dataset
* twitter_gen for the Twitter Random dataset

and third the filename (without file extension)

**Example:**
`java -jar tagrec.jar bll_c bib bib_sample`

## Input format
The input-files have to be placed in the corresponding subdirectory and are in csv-format (file extension: .txt) with 5 columns (quotation marks are mandatory):
* User
* Resource
* Timestamp in seconds
* List of tags
* List of categories (optional)

**Example:**
"0";"13830";"986470059";"deri,web2.0,tutorial,www,conference";""

There are three files needed:
* one file for training (with _train suffix)
* one file for testing (with _test suffix)
* one file that first contains the training-set and then the test-set (no suffix - is used for generating indices for the calculations)

**Example:**
bib_sample_train.txt, bib_sample_test.txt, bib_sample.txt (combination of train and test file)

## Output format
The output-file is generated in the corresponding subdirectory and is in csv-format with the following columns:
* Recall
* Precision
* F1-score
* Mean Reciprocal Rank
* Mean Average Precision
* Normalized Discounted Cummulative Gain
* User Coverage
* Diversity (in case of resource recommendations)
* Serendipity (in case of resource recommendations)

for _k_ = 1 to 10 (or 20) - each line is one _k_

**Example:**
0,5212146123336273;0,16408544726301685;0,22663857529082376 ...

## Citation

Kowald, D., Kopeinik, S., & Lex, E. (2017). The TagRec Framework as a Toolkit for the Development of Tag-Based Recommender Systems. In Adjunct Publication of the 25th Conference on User Modeling, Adapation and Personalization (UMAP'2017). ACM.

_Bibtex:_
`@inproceedings{kowaldumap2017,
 author = {Kowald, Dominik and Kopeinik, Simone and Lex, Elisabeth},
 title = {The TagRec Framework As a Toolkit for the Development of Tag-Based Recommender Systems},
 booktitle = {Adjunct Publication of the 25th Conference on User Modeling, Adaptation and Personalization},
 series = {UMAP '17},
 year = {2017},
 isbn = {978-1-4503-5067-9},
 location = {Bratislava, Slovakia},
 pages = {23--28},
 numpages = {6},
 url = {http://doi.acm.org/10.1145/3099023.3099069},
 doi = {10.1145/3099023.3099069},
 acmid = {3099069},
 publisher = {ACM},
 address = {New York, NY, USA},
 keywords = {hashtag recommendation, recommendation evaluation, recommender framework, recommender systems, tag recommendation}
}`

## Publications
* Kowald, D., Kopeinik, S., & Lex, E. (2017). The TagRec Framework as a Toolkit for the Development of Tag-Based Recommender Systems. In Adjunct Publication of the 25th Conference on User Modeling, Adapation and Personalization (UMAP'2017). ACM.
* Kowald, D., Pujari, S., & Lex, E. (2017). Temporal Effects on Hashtag Reuse in Twitter: A Cognitive-Inspired Hashtag Recommendation Approach. In Proceedings of the 26th International World Wide Web Conference (WWW'2017). ACM.
* D. Kowald and E. Lex: [The Influence of Frequency, Recency and Semantic Context on the Reuse of Tags in Social Tagging Systems](https://arxiv.org/pdf/1604.00837v1.pdf), In Proc. of Hypertext, 2016
* C. Trattner, D. Kowald, P. Seitlinger, S. Kopeinik and T. Ley: [Modeling Activation Processes in Human Memory to Predict the Use of Tags in Social Bookmarking Systems](http://www.christophtrattner.info/pubs/bll_journal_final.pdf), Journal of Web Science, 2016.
* D. Kowald and E. Lex: [Evaluating Tag Recommender Algorithms in Real-World Folksonomies: A Comparative Study](http://dl.acm.org/citation.cfm?id=2799664), In Proceedings of the 9th ACM Conference on Recommender Systems (RecSys 2015), ACM, New York, NY, USA, 2015.
* S. Larrain, C. Trattner, D. Parra, E. Graells-Garrido and K. Norvag: [Good Times Bad Times: A Study on Recency Effects in Collaborative Filtering for Social Tagging](http://www.christophtrattner.info/pubs/recsys2015b.pdf), In Proceedings of the 9th ACM Conference on Recommender Systems (RecSys 2015), ACM, New York, NY, USA, 2015.
* P. Seitlinger, D. Kowald, S. Kopeinik, I. Hasani-Mavriqi, T. Ley, and Elisabeth Lex: [Attention Please! A Hybrid Resource Recommender Mimicking Attention-Interpretation Dynamics](http://arxiv.org/pdf/1501.07716v1.pdf). In Proc. of WWW'2015 Companion. ACM. 2015
* D. Kowald, S. Kopeinik, P. Seitinger, T. Ley, D. Albert, and C. Trattner: [Refining Frequency-Based Tag Reuse Predictions by Means of Time and Semantic Context](http://www.christophtrattner.info/pubs/msm7_kowald.pdf). In Mining, Modeling, and Recommending 'Things' in Social Media, Lecture Notes in Computer Science, Vol. 8940, Springer, 2015a.
* D. Kowald, P. Seitinger, S. Kopeinik, T. Ley, and C. Trattner: [Forgetting the Words but Remembering the Meaning: Modeling Forgetting in a Verbal and Semantic Tag Recommender](http://www.christophtrattner.info/pubs/msm8_kowald.pdf). In Mining, Modeling, and Recommending 'Things' in Social Media, Lecture Notes in Computer Science, Vol. 8940, Springer, 2015b.
* D. Kowald, P. Seitlinger, C. Trattner, and T. Ley. [Long Time No See: The Probability of Reusing Tags as a Function of Frequency and Recency](http://www2014.kr/wp-content/uploads/2014/05/companion_p463.pdf). In Proceedings of the 23rd international conference on World Wide Web Companion, WWW '14, ACM, New York, NY, USA, 2014.
* E. Lacic, D. Kowald, P. Seitlinger, C. Trattner, and D. Parra. [Recommending Items in Social Tagging Systems Using Tag and Time Information](http://www.christophtrattner.info/pubs/sp2014.pdf). In Proceedings of the 1st Social Personalization Workshop co-located with the 25th ACM Conference on Hypertext and Social Media, HT'14, ACM, New York, NY, USA, 2014.
* P. Seitlinger, D. Kowald, C. Trattner, and T. Ley.: [Recommending Tags with a Model of Human Categorization](http://www.christophtrattner.info/pubs/cikm2013.pdf). In Proceedings of The ACM International Conference on Information and Knowledge Management (CIKM 2013), ACM, New York, NY, USA, 2013.

## References
* A. Hotho, R. Jäschke, C. Schmitz, and G. Stumme. Information retrieval in folksonomies: Search and ranking. In The semantic web: research and applications. Springer, 2006.
* L. Zhang, J. Tang, and M. Zhang. Integrating temporal usage pattern into personalized tag prediction. In Web Technologies and Applications. Springer, 2012.
* R. Jäschke, L. Marinho, A. Hotho, L. Schmidt-Thieme, and G. Stumme. Tag recommendations in folksonomies. In Knowledge Discovery in Databases: PKDD 2007. Springer, 2007.
* R. Krestel, P. Fankhauser, and W. Nejdl. Latent dirichlet allocation for tag recommendation. In Proceedings of the third ACM conference on Recommender systems. ACM, 2009.
* J. R. Anderson, M. D. Byrne, S. Douglass, C. Lebiere, and Y. Qin. An integrated theory of the mind. Psychological Review, 111(4), 2004.
* J. K. Kruschke et al. Alcove: An exemplar-based connectionist model of category learning. Psychological review, 99(1), 1992.
* D. L Hintzman. Minerva 2: A simulation model of human memory. Behavior Research Methods, Instruments, & Computers 16 (2), 1984.
* N. Zheng and Q. Li. A recommender system based on tag and time information for social tagging systems. Expert Syst. Appl., 2011.
* C.-L. Huang, P.-H. Yeh, C.-W. Lin, and D.-C. Wu. Utilizing user tag-based interests in recommender systems for social resource sharing websites. Knowledge-Based Systems, 2014.
* B. C. Love, D. L. Medin, and T. M. Gureckis. Sustain: A network model of category learning. Psychological review, 111(2):309, 2004.

## Main contact and contributor
* [Dominik Kowald](http://www.dominikkowald.info/), Know-Center, Graz University of Technology, dkowald [AT] know [MINUS] center [DOT] at (general contact)

## Contacts and contributors
* Simone Kopeinik, Knowledge Technologies Institute, Graz University of Technology, simone [DOT] [AT] tugraz [DOT] at (sustain resource recommender algorithm)
* Emanuel Lacic, Knowledge Technologies Institute, Graz University of Technology, elacic [AT] know [MINUS] center [DOT] at (huang, zheng and CIRTT resource recommender algorithms)
* Subhash Pujari, Knowledge Technologies Institute, Graz University of Technology, spujari [AT] student [DOT] tugraz [DOT] at (twitter hashtag recommender algorithms)
* Elisabeth Lex, Knowledge Technologies Institute, Graz University of Technology, elisabeth [DOT] lex [AT] tugraz [DOT] at (general contact)
* Christoph Trattner, Know-Center, Graz University of Technology, ctrattner [AT] know [MINUS] center [DOT] at (general contact)

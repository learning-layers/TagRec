TagRec
======

##Towards A Standardized Tag Recommender Benchmarking Framework

TagRec won the best poster award @ Hypertext 2014 (HT'14) conference: [http://ht.acm.org/ht2014/index.php?awards.poster](http://ht.acm.org/ht2014/index.php?awards.poster)

## Description
The aim of this [work](http://www.christophtrattner.info/pubs/ht241-kowald.pdf) (please [cite](https://github.com/learning-layers/TagRec/#citation)) is to provide the community with a simple to use, generic tag-recommender framework written in Java to evaluate novel tag-recommender algorithms with a set of well-known std. IR metrics such as nDCG, MAP, MRR, Precision (P@k), Recall (R@k), F1-score (F1@k), Diversity (D), Serendipity (S), User Coverage (UC) and folksonomy datasets such as BibSonomy, CiteULike, LastFM, Flickr, MovieLens or Delicious and to benchmark the developed approaches against state-of-the-art tag-recommender algorithms such as MP, MP_r, MP_u, MP_u,r, CF, APR, FR, GIRP, GIRPTM, etc.

Furthermore, it contains algorithms to process datasets (e.g., p-core pruning, leave-one-out or 80/20 splitting, LDA topic creation and create input files for other recommender frameworks).

The software already contains four novel tag-recommender approaches based on cognitive science theory. The first one ([3Layers](http://www.christophtrattner.info/pubs/cikm2013.pdf)) (Seitlinger et al, 2013) uses topic information and is based on the ALCOVE/MINERVA2 theories (Krutschke, 1992; Hintzman, 1984). The second one ([BLL+C](http://delivery.acm.org/10.1145/2580000/2576934/p463-kowald.pdf)) (Kowald et al., 2014b) uses time information is based on the ACT-R theory (Anderson et al., 2004). The third one ([3LT](http://www.christophtrattner.info/pubs/msm8_kowald.pdf)) (Kowald et al., 2015b) is a combination of the former two approaches and integrates the time component on the level of tags and topics. Finally, the fourth one ([BLLac+MPr](http://www.christophtrattner.info/pubs/msm7_kowald.pdf)) extends the BLL+C algorithm with semantic correlations (Kowald et al., 2015a).

Based on our latest strand of research, TagRec also contains algorithms for the personalized recommendation of resources / items in social tagging systems. In this respect TagRec includes a novel algorithm called [CIRTT](http://www.christophtrattner.info/pubs/sp2014.pdf) (Lacic et al., 2014) that integrates tag and time information using the BLL-equation coming from the ACT-R theory (Anderson et al, 2004). Furthermore, it contains another novel item-recommender called [SUSTAIN+CFu](http://arxiv.org/pdf/1501.07716v1.pdf) (Seitlinger et al., 2015) that improves user-based CF via integrating the addentional focus of users via the SUSTAIN model (Love et al., 2004).

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

## How-to-use
The _tagrecommender_ .jar uses three parameters:

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

Data-Processing:
* core for calculating p-cores on a dataset
* split_l1o for splitting a dataset into training and test-sets using a leave-one-out method
* split_8020 for splitting a dataset into training and test-sets using 80/20 split
* lda_samples for creating LDA topics for the resources in a dataset
* tensor_samples for creating samples for the FM and PITF methods implemented in [PITF and FM algorithms](http://www.informatik.uni-konstanz.de/rendle/software/tag-recommender/)
* mymedialite_samples for creating samples for the WRMF method implemented in [MyMediaLite](http://www.mymedialite.net/)

, second the dataset(-directory):
* bib for BibSonomy
* cul for CiteULike
* del for Delicious
* flickr for Flickr
* ml for MovieLens
* lastfm for LastFM
* wiki for Wikipedia (based on bookmarks from Delicious)

and third the filename (without file extension)

**Example:**
`java -jar tagrecommender.jar bll_c bib bib_sample`

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
D. Kowald, E. Lacic, and C. Trattner. [Tagrec:Towards a standardized tag recommender benchmarking framework](http://www.christophtrattner.info/pubs/ht241-kowald.pdf). In Proceedings of the 25th ACM Conference on Hypertext and Social Media, HT'14, New York, NY, USA, 2014. ACM.

_Bibtex:_
`@inproceedings{Kowald2014TagRec,
 author = {Kowald, Dominik and Lacic, Emanuel and Trattner, Christoph},
 title = {TagRec: Towards A Standardized Tag Recommender Benchmarking Framework},
 booktitle = {Proceedings of the 25th ACM Conference on Hypertext and Social Media},
 series = {HT '14},
 year = {2014},
 isbn = {978-1-4503-2263-8},
 location = {Santiago de Chile, Chile},
 publisher = {ACM},
 address = {New York, NY, USA},
}`

C. Trattner, D. Kowald and E. Lacic: [TagRec: Towards a Toolkit for Reproducible Evaluation and Development of Tag-Based Recommender Algorithms](http://www.christophtrattner.info/pubs/sigweb2015.pdf), ACM SIGWEB Newsletter, Spring 2015, ACM, New York, NY, USA, 2015. (invited)

_Bibtex:_
`@article{Trattner:2015:TTT:2719943.2719946,
 author = {Trattner, Christoph and Kowald, Dominik and Lacic, Emanuel},
 title = {TagRec: Towards a Toolkit for Reproducible Evaluation and Development of Tag-based Recommender Algorithms},
 journal = {SIGWEB Newsl.},
 issue_date = {Winter 2015},
 year = {2015},
 pages = {3:1--3:10},
 numpages = {10},
 publisher = {ACM},
 address = {New York, NY, USA},
}`

## References
* P. Seitlinger, D. Kowald, S. Kopeinik, I. Hasani-Mavriqi, T. Ley, and Elisabeth Lex: [Attention Please! A Hybrid Resource Recommender Mimicking Attention-Interpretation Dynamics](http://arxiv.org/pdf/1501.07716v1.pdf). Under review. 2015.
* D. Kowald, S. Kopeinik, P. Seitinger, T. Ley, D. Albert, and C. Trattner: [Refining Frequency-Based Tag Reuse Predictions by Means of Time and Semantic Context](http://www.christophtrattner.info/pubs/msm7_kowald.pdf). In Mining, Modeling, and Recommending 'Things' in Social Media, Lecture Notes in Computer Science, Vol. 8940, Springer, 2015a.
* D. Kowald, P. Seitinger, S. Kopeinik, T. Ley, and C. Trattner: [Forgetting the Words but Remembering the Meaning: Modeling Forgetting in a Verbal and Semantic Tag Recommender](http://www.christophtrattner.info/pubs/msm8_kowald.pdf). In Mining, Modeling, and Recommending 'Things' in Social Media, Lecture Notes in Computer Science, Vol. 8940, Springer, 2015b.
* D. Kowald, P. Seitlinger, C. Trattner, and T. Ley. [Long Time No See: The Probability of Reusing Tags as a Function of Frequency and Recency](http://www2014.kr/wp-content/uploads/2014/05/companion_p463.pdf). In Proceedings of the 23rd international conference on World Wide Web Companion, WWW '14, ACM, New York, NY, USA, 2014.
* E. Lacic, D. Kowald, P. Seitlinger, C. Trattner, and D. Parra. [Recommending Items in Social Tagging Systems Using Tag and Time Information](http://www.christophtrattner.info/pubs/sp2014.pdf). In Proceedings of the 1st Social Personalization Workshop co-located with the 25th ACM Conference on Hypertext and Social Media, HT'14, ACM, New York, NY, USA, 2014.
* P. Seitlinger, D. Kowald, C. Trattner, and T. Ley.: [Recommending Tags with a Model of Human Categorization](http://www.christophtrattner.info/pubs/cikm2013.pdf). In Proceedings of The ACM International Conference on Information and Knowledge Management (CIKM 2013), ACM, New York, NY, USA, 2013.
* A. Hotho, R. Jäschke, C. Schmitz, and G. Stumme. Information retrieval in folksonomies: Search and ranking. In The semantic web: research and applications, pages 411–426. Springer, 2006.
* L. Zhang, J. Tang, and M. Zhang. Integrating temporal usage pattern into personalized tag prediction. In Web Technologies and Applications, pages 354–365. Springer, 2012.
* R. Jäschke, L. Marinho, A. Hotho, L. Schmidt-Thieme, and G. Stumme. Tag recommendations in folksonomies. In Knowledge Discovery in Databases: PKDD 2007, pages 506–514. Springer, 2007.
* R. Krestel, P. Fankhauser, and W. Nejdl. Latent dirichlet allocation for tag recommendation. In Proceedings of the third ACM conference on Recommender systems, pages 61–68. ACM, 2009.
* J. R. Anderson, M. D. Byrne, S. Douglass, C. Lebiere, and Y. Qin. An integrated theory of the mind. Psychological Review, 111(4):1036–1050, 2004.
* J. K. Kruschke et al. Alcove: An exemplar-based connectionist model of category learning. Psychological review, 99(1):22–44, 1992.
* D. L Hintzman. Minerva 2: A simulation model of human memory. Behavior Research Methods, Instruments, & Computers 16 (2), 96–101, 1984.
* N. Zheng and Q. Li. A recommender system based on tag and time information for social tagging systems. Expert Syst. Appl., 2011.
* C.-L. Huang, P.-H. Yeh, C.-W. Lin, and D.-C. Wu. Utilizing user tag-based interests in recommender systems for social resource sharing websites. Knowledge-Based Systems, 2014.
* B. C. Love, D. L. Medin, and T. M. Gureckis. Sustain: A network model of category learning. Psychological review, 111(2):309, 2004.

## Main contributor
* Dominik Kowald, Know-Center, Graz University of Technology, dkowald@know-center.at

## Contacts and contributors (in alphabetically order)
* Simone Kopeinik, Knowledge Technologies Institute, Graz University of Technology, simone.kopeinik@tugraz.at (sustain resource recommender algorithm)
* Emanuel Lacic, Knowledge Technologies Institute, Graz University of Technology, elacic@know-center.at (huang, zheng and CIRTT resource recommender algorithms)
* Elisabeth Lex, Knowledge Technologies Institute, Graz University of Technology, elisabeth.lex@tugraz.at (general contact)
* Christoph Trattner, Norwegian University of Science and Technology Trondheim, chritrat@idi.ntnu.no (general contact)
